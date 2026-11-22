package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.FlightOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.dto.OfferDtos.FlightOfferRequest;
import tn.tunisieholidays.dto.OfferDtos.FlightOfferView;
import tn.tunisieholidays.repository.FlightOfferRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FlightOfferHandler implements OfferHandler<FlightOffer, FlightOfferRequest> {

    private final FlightOfferRepository repository;

    public OfferType type() {
        return OfferType.FLIGHT;
    }

    public Set<PartnerType> allowedPartnerTypes() {
        return Set.of(PartnerType.AGENCY);
    }

    public Class<FlightOfferRequest> requestType() {
        return FlightOfferRequest.class;
    }

    public JpaRepository<FlightOffer, Long> repository() {
        return repository;
    }

    public List<FlightOffer> findByPartner(Long partnerId) {
        return repository.findByPartnerIdFrom(partnerId, LocalDate.now().minusDays(7).atStartOfDay());
    }

    public FlightOffer newEntity() {
        return new FlightOffer();
    }

    public void apply(FlightOffer e, FlightOfferRequest r) {
        if (!r.arrivalAt().isAfter(r.departureAt())) {
            throw ApiException.badRequest("L'arrivée doit suivre le départ");
        }
        if (r.originCode().equals(r.destinationCode())) {
            throw ApiException.badRequest("L'origine et la destination doivent être différentes");
        }
        e.setAirlineCode(r.airlineCode());
        e.setAirlineName(r.airlineName());
        e.setFlightNumber(r.flightNumber());
        e.setOriginCode(r.originCode());
        e.setDestinationCode(r.destinationCode());
        e.setDepartureAt(r.departureAt());
        e.setArrivalAt(r.arrivalAt());
        e.setStops(r.stops());
        e.setCabin(r.cabin());
        e.setBaggageIncluded(r.baggageIncluded());
        e.setSeatsAvailable(r.seatsAvailable());
    }

    public Object toView(FlightOffer e, long clicks) {
        return new FlightOfferView(e.getId(), e.isActive(), clicks, e.getAirlineCode(), e.getAirlineName(),
                e.getFlightNumber(), e.getOriginCode(), e.getDestinationCode(), e.getDepartureAt(), e.getArrivalAt(),
                e.getStops(), e.getCabin(), e.isBaggageIncluded(), e.getSeatsAvailable(), e.getPrice(),
                e.getCurrency(), e.getDeeplinkUrl());
    }
}
