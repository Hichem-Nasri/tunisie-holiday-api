package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.dto.PartnerDtos.PartnerUpdateRequest;
import tn.tunisieholidays.dto.PartnerDtos.PartnerView;
import tn.tunisieholidays.dto.StatsDtos.PartnerStats;
import tn.tunisieholidays.repository.ClickEventRepository;
import tn.tunisieholidays.repository.PartnerRepository;
import tn.tunisieholidays.security.AuthPrincipal;
import tn.tunisieholidays.service.StatsService;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository repository;
    private final ClickEventRepository clickRepository;
    private final StatsService statsService;
    private final PartnerOfferService offerService;

    @Transactional(readOnly = true)
    public PartnerView me(AuthPrincipal principal) {
        return PartnerView.of(require(principal));
    }

    @Transactional
    public PartnerView update(AuthPrincipal principal, PartnerUpdateRequest request) {
        Partner p = require(principal);
        p.setName(request.name());
        p.setPhone(request.phone());
        p.setWebsite(request.website());
        p.setCity(request.city());
        p.setLogoUrl(request.logoUrl());
        p.setDescription(request.description());
        return PartnerView.of(repository.save(p));
    }

    @Transactional(readOnly = true)
    public PartnerStats stats(AuthPrincipal principal) {
        Partner p = require(principal);
        return new PartnerStats(p.getStatus().name(),
                clickRepository.countByPartnerIdAndCreatedAtAfter(p.getId(), StatsService.since30Days()),
                offerService.countOffers(p.getId()),
                statsService.perDay(p.getId()),
                statsService.perType(p.getId()));
    }

    private Partner require(AuthPrincipal principal) {
        if (principal.partnerId() == null) {
            throw ApiException.forbidden("Aucun compte partenaire associé");
        }
        return repository.findById(principal.partnerId())
                .orElseThrow(() -> ApiException.notFound("Compte partenaire introuvable"));
    }
}
