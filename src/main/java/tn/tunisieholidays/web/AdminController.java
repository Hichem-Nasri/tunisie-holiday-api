package tn.tunisieholidays.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.CatalogDtos.CurrencyView;
import tn.tunisieholidays.dto.CatalogDtos.DestinationRequest;
import tn.tunisieholidays.dto.CatalogDtos.DestinationView;
import tn.tunisieholidays.dto.CatalogDtos.RateUpdateRequest;
import tn.tunisieholidays.dto.OfferDtos.HotelRequest;
import tn.tunisieholidays.dto.OfferDtos.HotelView;
import tn.tunisieholidays.dto.PartnerDtos.PartnerStatusRequest;
import tn.tunisieholidays.dto.PartnerDtos.PartnerView;
import tn.tunisieholidays.dto.StatsDtos.AdminStats;
import tn.tunisieholidays.service.AdminService;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.DestinationService;
import tn.tunisieholidays.service.partner.HotelCatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DestinationService destinationService;
    private final CurrencyService currencyService;
    private final HotelCatalogService hotelCatalogService;

    @GetMapping("/stats")
    public AdminStats stats() {
        return adminService.stats();
    }

    @GetMapping("/partners")
    public List<PartnerView> partners(@RequestParam(required = false) PartnerStatus status) {
        return adminService.partners(status);
    }

    @PatchMapping("/partners/{id}/status")
    public PartnerView updateStatus(@PathVariable Long id, @Valid @RequestBody PartnerStatusRequest request) {
        return adminService.updateStatus(id, request.status());
    }

    @PostMapping("/destinations")
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationView createDestination(@Valid @RequestBody DestinationRequest request) {
        return destinationService.create(request);
    }

    @PutMapping("/destinations/{id}")
    public DestinationView updateDestination(@PathVariable Long id, @Valid @RequestBody DestinationRequest request) {
        return destinationService.update(id, request);
    }

    @PutMapping("/currencies/{code}")
    public CurrencyView updateRate(@PathVariable String code, @Valid @RequestBody RateUpdateRequest request) {
        return currencyService.updateRate(code, request.rateToTnd());
    }

    @GetMapping("/hotels")
    public List<HotelView> hotels(@RequestParam(required = false) String q) {
        return hotelCatalogService.search(q);
    }

    @PutMapping("/hotels/{id}")
    public HotelView updateHotel(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return hotelCatalogService.update(id, request);
    }
}
