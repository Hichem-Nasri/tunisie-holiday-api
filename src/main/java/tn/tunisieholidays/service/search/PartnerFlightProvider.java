package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.domain.FlightOffer;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.SearchDtos.FlightLeg;
import tn.tunisieholidays.dto.SearchDtos.FlightOption;
import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.repository.FlightOfferRepository;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PartnerFlightProvider implements FlightProvider {

    private static final int MAX_PAIRS = 300;

    private final FlightOfferRepository repository;

    @Override
    public String name() {
        return "PARTNERS";
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public List<FlightResult> search(FlightCriteria c, Converter converter) {
        List<FlightOffer> outbound = find(c.originAirports(), c.destinationAirports(), c.departureDate(), c);
        List<FlightResult> results = new ArrayList<>();
        if (!c.roundTrip()) {
            for (FlightOffer o : outbound) {
                FlightLeg leg = leg(o);
                BigDecimal price = converter.convert(o.getPrice(), o.getCurrency());
                FlightOption option = new FlightOption("P-" + o.getId(), "PARTNER", o.getPartner().getName(), price,
                        converter.target(), o.isBaggageIncluded(), "/api/go/FLIGHT/" + o.getId());
                results.add(new FlightResult(FlightKeys.key(o.getAirlineCode(), leg, null), o.getAirlineCode(),
                        o.getAirlineName(), leg, null, List.of(option), price, converter.target(),
                        leg.durationMinutes(), leg.stops()));
            }
            return results;
        }
        List<FlightOffer> inbound = find(c.destinationAirports(), c.originAirports(), c.returnDate(), c);
        for (FlightOffer o : outbound) {
            for (FlightOffer i : inbound) {
                if (results.size() >= MAX_PAIRS) {
                    return results;
                }
                if (!Objects.equals(o.getPartner().getId(), i.getPartner().getId())
                        || !o.getAirlineCode().equals(i.getAirlineCode())
                        || !i.getDepartureAt().isAfter(o.getArrivalAt())) {
                    continue;
                }
                FlightLeg out = leg(o);
                FlightLeg in = leg(i);
                BigDecimal price = converter.convert(o.getPrice(), o.getCurrency())
                        .add(converter.convert(i.getPrice(), i.getCurrency()));
                FlightOption option = new FlightOption("P-" + o.getId() + "-" + i.getId(), "PARTNER",
                        o.getPartner().getName(), price, converter.target(),
                        o.isBaggageIncluded() && i.isBaggageIncluded(),
                        "/api/go/FLIGHT/" + o.getId() + "?returnOfferId=" + i.getId());
                results.add(new FlightResult(FlightKeys.key(o.getAirlineCode(), out, in), o.getAirlineCode(),
                        o.getAirlineName(), out, in, List.of(option), price, converter.target(),
                        out.durationMinutes() + in.durationMinutes(), Math.max(out.stops(), in.stops())));
            }
        }
        return results;
    }

    private List<FlightOffer> find(Set<String> from, Set<String> to, LocalDate date, FlightCriteria c) {
        return repository.search(from, to, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), c.adults(),
                c.cabin(), PartnerStatus.ACTIVE);
    }

    private FlightLeg leg(FlightOffer o) {
        long minutes = Duration.between(o.getDepartureAt(), o.getArrivalAt()).toMinutes();
        return new FlightLeg(o.getOriginCode(), o.getDestinationCode(), o.getDepartureAt(), o.getArrivalAt(),
                minutes, o.getStops(), o.getFlightNumber());
    }
}
