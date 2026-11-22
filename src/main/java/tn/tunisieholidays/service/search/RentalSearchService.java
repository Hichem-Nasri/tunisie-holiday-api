package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.domain.RentalOffer;
import tn.tunisieholidays.domain.RentalType;
import tn.tunisieholidays.dto.SearchDtos.RentalResult;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.repository.RentalOfferRepository;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;
import tn.tunisieholidays.service.DestinationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentalSearchService {

    private final RentalOfferRepository repository;
    private final DestinationService destinationService;
    private final CurrencyService currencyService;

    public record Criteria(String query, LocalDate checkIn, LocalDate checkOut, int guests, String currency,
                           Set<RentalType> types, boolean poolOnly, boolean seaViewOnly, BigDecimal maxPricePerNight) {
    }

    @Transactional(readOnly = true)
    public SearchResponse<RentalResult> search(Criteria c) {
        int nights = Stays.nights(c.checkIn(), c.checkOut());
        Converter converter = currencyService.converter(c.currency());
        List<Long> destinationIds = destinationService.resolveIds(c.query());
        if (destinationIds.isEmpty()) {
            return new SearchResponse<>(List.of(), 0, converter.target(), nights, null, List.of("PARTNERS"));
        }
        List<RentalResult> results = repository.findActiveByDestinations(destinationIds, PartnerStatus.ACTIVE).stream()
                .filter(o -> o.getCapacity() >= Math.max(1, c.guests()))
                .filter(o -> o.getMinNights() <= nights)
                .filter(o -> c.types() == null || c.types().isEmpty() || c.types().contains(o.getType()))
                .filter(o -> !c.poolOnly() || o.isPool())
                .filter(o -> !c.seaViewOnly() || o.isSeaView())
                .map(o -> toResult(o, nights, converter))
                .filter(r -> c.maxPricePerNight() == null || r.pricePerNight().compareTo(c.maxPricePerNight()) <= 0)
                .sorted(Comparator.comparing(RentalResult::totalPrice))
                .toList();
        return new SearchResponse<>(results, results.size(), converter.target(), nights, null, List.of("PARTNERS"));
    }

    private RentalResult toResult(RentalOffer o, int nights, Converter converter) {
        BigDecimal perNight = converter.convert(o.getPrice(), o.getCurrency());
        return new RentalResult(o.getId(), o.getPartner().getName(), o.getTitle(), o.getType(),
                o.getDestination().getName(), o.getCapacity(), o.getBedrooms(), o.getBathrooms(), o.isPool(),
                o.isWifi(), o.isSeaView(), o.getMinNights(), o.getDescription(), o.getImageUrl(), perNight,
                perNight.multiply(BigDecimal.valueOf(nights)), converter.target(), "/api/go/RENTAL/" + o.getId());
    }
}
