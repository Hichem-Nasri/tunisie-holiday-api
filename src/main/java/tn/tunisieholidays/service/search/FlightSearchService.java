package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Cabin;
import tn.tunisieholidays.dto.SearchDtos.FlightOption;
import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.service.AirportService;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

@Service
@RequiredArgsConstructor
public class FlightSearchService {

    public enum Sort { PRICE, DURATION, BEST }

    private final List<FlightProvider> providers;
    private final AirportService airportService;
    private final CurrencyService currencyService;

    @Transactional(readOnly = true)
    public SearchResponse<FlightResult> search(String origin, String destination, LocalDate departureDate,
                                               LocalDate returnDate, int adults, Cabin cabin, String currency,
                                               Integer maxStops, BigDecimal maxPrice, Sort sort) {
        if (departureDate.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("La date de départ est passée");
        }
        if (returnDate != null && returnDate.isBefore(departureDate)) {
            throw ApiException.badRequest("La date de retour doit suivre la date de départ");
        }
        if (adults < 1 || adults > 9) {
            throw ApiException.badRequest("Le nombre de voyageurs doit être compris entre 1 et 9");
        }
        String from = origin.trim().toUpperCase(Locale.ROOT);
        String to = destination.trim().toUpperCase(Locale.ROOT);
        FlightCriteria criteria = new FlightCriteria(from, to, airportService.expand(from), airportService.expand(to),
                departureDate, returnDate, adults, cabin == null ? Cabin.ECONOMY : cabin);
        Converter converter = currencyService.converter(currency);

        List<String> sources = new ArrayList<>();
        Map<String, List<FlightOption>> options = new LinkedHashMap<>();
        Map<String, FlightResult> firstByKey = new LinkedHashMap<>();
        for (FlightProvider provider : providers) {
            if (!provider.enabled()) {
                continue;
            }
            sources.add(provider.name());
            for (FlightResult r : provider.search(criteria, converter)) {
                firstByKey.putIfAbsent(r.id(), r);
                options.computeIfAbsent(r.id(), k -> new ArrayList<>()).addAll(r.offers());
            }
        }

        List<FlightResult> merged = new ArrayList<>();
        for (Map.Entry<String, FlightResult> e : firstByKey.entrySet()) {
            FlightResult base = e.getValue();
            List<FlightOption> sorted = options.get(e.getKey()).stream()
                    .sorted(Comparator.comparing(FlightOption::price))
                    .toList();
            merged.add(new FlightResult(base.id(), base.airlineCode(), base.airlineName(), base.outbound(),
                    base.inbound(), sorted, sorted.get(0).price(), converter.target(), base.totalDurationMinutes(),
                    base.maxStops()));
        }

        List<FlightResult> filtered = merged.stream()
                .filter(r -> maxStops == null || r.maxStops() <= maxStops)
                .filter(r -> maxPrice == null || r.minPrice().compareTo(maxPrice) <= 0)
                .sorted(comparator(sort == null ? Sort.PRICE : sort, merged))
                .toList();
        return new SearchResponse<>(filtered, filtered.size(), converter.target(), null, null, sources);
    }

    static Comparator<FlightResult> comparator(Sort sort, List<FlightResult> all) {
        return switch (sort) {
            case PRICE -> Comparator.comparing(FlightResult::minPrice)
                    .thenComparingLong(FlightResult::totalDurationMinutes);
            case DURATION -> Comparator.comparingLong(FlightResult::totalDurationMinutes)
                    .thenComparing(FlightResult::minPrice);
            case BEST -> {
                double minPrice = all.stream().map(FlightResult::minPrice).min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ONE).max(BigDecimal.ONE).doubleValue();
                double minDuration = Math.max(1, all.stream().mapToLong(FlightResult::totalDurationMinutes).min().orElse(1));
                ToDoubleFunction<FlightResult> score = r -> r.minPrice().setScale(2, RoundingMode.HALF_UP).doubleValue() / minPrice
                        + r.totalDurationMinutes() / minDuration;
                yield Comparator.comparingDouble(score);
            }
        };
    }
}
