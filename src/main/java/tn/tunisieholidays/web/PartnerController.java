package tn.tunisieholidays.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.OfferDtos.HotelRequest;
import tn.tunisieholidays.dto.OfferDtos.HotelView;
import tn.tunisieholidays.dto.PartnerDtos.PartnerUpdateRequest;
import tn.tunisieholidays.dto.PartnerDtos.PartnerView;
import tn.tunisieholidays.dto.StatsDtos.PartnerStats;
import tn.tunisieholidays.security.AuthPrincipal;
import tn.tunisieholidays.service.partner.HotelCatalogService;
import tn.tunisieholidays.service.partner.PartnerOfferService;
import tn.tunisieholidays.service.partner.PartnerService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partner")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;
    private final PartnerOfferService offerService;
    private final HotelCatalogService hotelCatalogService;

    @GetMapping("/me")
    public PartnerView me(@AuthenticationPrincipal AuthPrincipal principal) {
        return partnerService.me(principal);
    }

    @PutMapping("/me")
    public PartnerView update(@AuthenticationPrincipal AuthPrincipal principal,
                              @Valid @RequestBody PartnerUpdateRequest request) {
        return partnerService.update(principal, request);
    }

    @GetMapping("/stats")
    public PartnerStats stats(@AuthenticationPrincipal AuthPrincipal principal) {
        return partnerService.stats(principal);
    }

    @GetMapping("/offers/{type}")
    public List<Object> list(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable OfferType type) {
        return offerService.list(principal, type);
    }

    @PostMapping("/offers/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public Object create(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable OfferType type,
                         @RequestBody JsonNode body) {
        return offerService.create(principal, type, body);
    }

    @PutMapping("/offers/{type}/{id}")
    public Object update(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable OfferType type,
                         @PathVariable Long id, @RequestBody JsonNode body) {
        return offerService.update(principal, type, id, body);
    }

    @PatchMapping("/offers/{type}/{id}/active")
    public Object setActive(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable OfferType type,
                            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) {
            throw ApiException.badRequest("Le champ active est obligatoire");
        }
        return offerService.setActive(principal, type, id, active);
    }

    @DeleteMapping("/offers/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable OfferType type,
                       @PathVariable Long id) {
        offerService.delete(principal, type, id);
    }

    @GetMapping("/hotels")
    public List<HotelView> hotels(@RequestParam(required = false) String q) {
        return hotelCatalogService.search(q);
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelView createHotel(@AuthenticationPrincipal AuthPrincipal principal,
                                 @Valid @RequestBody HotelRequest request) {
        PartnerView partner = partnerService.me(principal);
        if (partner.status() != PartnerStatus.ACTIVE) {
            throw ApiException.forbidden("Votre compte partenaire doit être validé avant d'ajouter un hôtel");
        }
        return hotelCatalogService.create(request);
    }
}
