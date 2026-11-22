package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.PartnerDtos.PartnerView;
import tn.tunisieholidays.dto.StatsDtos.AdminStats;
import tn.tunisieholidays.repository.ClickEventRepository;
import tn.tunisieholidays.repository.PartnerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PartnerRepository partnerRepository;
    private final ClickEventRepository clickRepository;
    private final StatsService statsService;

    @Transactional(readOnly = true)
    public List<PartnerView> partners(PartnerStatus status) {
        List<Partner> partners = status == null
                ? partnerRepository.findAllByOrderByCreatedAtDesc()
                : partnerRepository.findByStatusOrderByCreatedAtDesc(status);
        return partners.stream().map(PartnerView::of).toList();
    }

    @Transactional
    public PartnerView updateStatus(Long id, PartnerStatus status) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Partenaire introuvable"));
        partner.setStatus(status);
        return PartnerView.of(partnerRepository.save(partner));
    }

    @Transactional(readOnly = true)
    public AdminStats stats() {
        return new AdminStats(
                partnerRepository.countByStatus(PartnerStatus.ACTIVE),
                partnerRepository.countByStatus(PartnerStatus.PENDING),
                clickRepository.countByCreatedAtAfter(StatsService.since30Days()),
                statsService.perDay(null),
                statsService.perType(null),
                statsService.perPartner().stream().limit(10).toList());
    }
}
