package tn.tunisieholidays.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.config.AppProperties;
import tn.tunisieholidays.domain.BaseOffer;
import tn.tunisieholidays.domain.ClickEvent;
import tn.tunisieholidays.domain.FlightOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.repository.ActivityOfferRepository;
import tn.tunisieholidays.repository.CarOfferRepository;
import tn.tunisieholidays.repository.ClickEventRepository;
import tn.tunisieholidays.repository.FlightOfferRepository;
import tn.tunisieholidays.repository.HotelOfferRepository;
import tn.tunisieholidays.repository.RentalOfferRepository;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClickService {

    private static final String IATA = "^[A-Z]{3}$";

    private final ClickEventRepository clickRepository;
    private final FlightOfferRepository flightRepository;
    private final HotelOfferRepository hotelRepository;
    private final CarOfferRepository carRepository;
    private final RentalOfferRepository rentalRepository;
    private final ActivityOfferRepository activityRepository;
    private final AppProperties properties;

    @Transactional
    public URI redirectToOffer(OfferType type, Long offerId, Long returnOfferId, HttpServletRequest request) {
        BaseOffer offer = findOffer(type, offerId)
                .filter(BaseOffer::isActive)
                .filter(o -> o.getPartner().getStatus() == PartnerStatus.ACTIVE)
                .orElseThrow(() -> ApiException.notFound("Offre indisponible"));
        record(type, offerId, offer.getPartner().getId(), "PARTNER", request);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(offer.getDeeplinkUrl());
        if (type == OfferType.FLIGHT && returnOfferId != null) {
            flightRepository.findById(returnOfferId)
                    .filter(r -> r.getPartner().getId().equals(offer.getPartner().getId()))
                    .ifPresent(r -> appendReturn(builder, r));
        }
        try {
            return builder.build(true).toUri();
        } catch (IllegalArgumentException ex) {
            return builder.encode().build().toUri();
        }
    }

    @Transactional
    public URI redirectToAmadeus(String origin, String destination, LocalDate departureDate, LocalDate returnDate,
                                 int adults, HttpServletRequest request) {
        if (origin == null || destination == null || !origin.matches(IATA) || !destination.matches(IATA)) {
            throw ApiException.badRequest("Codes aéroport invalides");
        }
        if (adults < 1 || adults > 9) {
            throw ApiException.badRequest("Nombre de voyageurs invalide");
        }
        String template = properties.amadeus().bookingUrlTemplate();
        String url = template
                .replace("{origin}", origin)
                .replace("{destination}", destination)
                .replace("{departureDate}", departureDate.toString())
                .replace("{adults}", String.valueOf(adults));
        url = returnDate == null
                ? url.replace("/{returnDate}", "").replace("{returnDate}", "")
                : url.replace("{returnDate}", returnDate.toString());
        record(OfferType.FLIGHT, null, null, "AMADEUS", request);
        return URI.create(url);
    }

    private void appendReturn(UriComponentsBuilder builder, FlightOffer inbound) {
        builder.queryParam("th_retour", inbound.getFlightNumber() == null ? "" : inbound.getFlightNumber().replace(" ", ""));
        builder.queryParam("th_retour_date", inbound.getDepartureAt().toLocalDate().toString());
    }

    private Optional<? extends BaseOffer> findOffer(OfferType type, Long id) {
        return switch (type) {
            case FLIGHT -> flightRepository.findById(id);
            case HOTEL -> hotelRepository.findById(id);
            case CAR -> carRepository.findById(id);
            case RENTAL -> rentalRepository.findById(id);
            case ACTIVITY -> activityRepository.findById(id);
        };
    }

    private void record(OfferType type, Long offerId, Long partnerId, String source, HttpServletRequest request) {
        ClickEvent event = new ClickEvent();
        event.setOfferType(type);
        event.setOfferId(offerId);
        event.setPartnerId(partnerId);
        event.setSource(source);
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
        event.setUserAgent(userAgent.length() > 300 ? userAgent.substring(0, 300) : userAgent);
        event.setVisitorHash(hash(request.getRemoteAddr() + "|" + userAgent + "|" + LocalDate.now()));
        clickRepository.save(event);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((value + properties.jwt().secret()).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes).substring(0, 32);
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }
}
