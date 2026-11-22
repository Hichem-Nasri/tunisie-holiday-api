package tn.tunisieholidays.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisieholidays.dto.CatalogDtos.AirportView;
import tn.tunisieholidays.dto.CatalogDtos.CurrencyView;
import tn.tunisieholidays.dto.CatalogDtos.DealView;
import tn.tunisieholidays.dto.CatalogDtos.DestinationView;
import tn.tunisieholidays.dto.CatalogDtos.PriceAlertRequest;
import tn.tunisieholidays.service.AirportService;
import tn.tunisieholidays.service.AlertService;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.DestinationService;
import tn.tunisieholidays.service.search.DealService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

    private final DestinationService destinationService;
    private final AirportService airportService;
    private final CurrencyService currencyService;
    private final DealService dealService;
    private final AlertService alertService;

    @GetMapping("/destinations")
    public List<DestinationView> destinations() {
        return destinationService.list();
    }

    @GetMapping("/destinations/{slug}")
    public DestinationView destination(@PathVariable String slug) {
        return destinationService.get(slug);
    }

    @GetMapping("/airports")
    public List<AirportView> airports(@RequestParam(required = false) String q) {
        return airportService.search(q);
    }

    @GetMapping("/currencies")
    public List<CurrencyView> currencies() {
        return currencyService.list();
    }

    @GetMapping("/deals")
    public List<DealView> deals(@RequestParam(defaultValue = "TND") String currency) {
        return dealService.deals(currency);
    }

    @PostMapping("/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createAlert(@Valid @RequestBody PriceAlertRequest request) {
        return Map.of("id", alertService.create(request));
    }
}
