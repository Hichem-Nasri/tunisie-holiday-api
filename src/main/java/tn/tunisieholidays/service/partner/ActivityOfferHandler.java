package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.domain.ActivityOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.dto.OfferDtos.ActivityOfferRequest;
import tn.tunisieholidays.dto.OfferDtos.ActivityOfferView;
import tn.tunisieholidays.repository.ActivityOfferRepository;
import tn.tunisieholidays.service.DestinationService;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ActivityOfferHandler implements OfferHandler<ActivityOffer, ActivityOfferRequest> {

    private final ActivityOfferRepository repository;
    private final DestinationService destinationService;

    public OfferType type() {
        return OfferType.ACTIVITY;
    }

    public Set<PartnerType> allowedPartnerTypes() {
        return Set.of(PartnerType.ACTIVITY, PartnerType.AGENCY);
    }

    public Class<ActivityOfferRequest> requestType() {
        return ActivityOfferRequest.class;
    }

    public JpaRepository<ActivityOffer, Long> repository() {
        return repository;
    }

    public List<ActivityOffer> findByPartner(Long partnerId) {
        return repository.findByPartnerId(partnerId);
    }

    public ActivityOffer newEntity() {
        return new ActivityOffer();
    }

    public void apply(ActivityOffer e, ActivityOfferRequest r) {
        e.setDestination(destinationService.require(r.destinationId()));
        e.setTitle(r.title());
        e.setCategory(r.category());
        e.setDurationHours(r.durationHours());
        e.setMeetingPoint(r.meetingPoint());
        e.setDescription(r.description());
        e.setImageUrl(r.imageUrl());
    }

    public Object toView(ActivityOffer e, long clicks) {
        return new ActivityOfferView(e.getId(), e.isActive(), clicks, e.getDestination().getId(),
                e.getDestination().getName(), e.getTitle(), e.getCategory(), e.getDurationHours(),
                e.getMeetingPoint(), e.getDescription(), e.getImageUrl(), e.getPrice(), e.getCurrency(),
                e.getDeeplinkUrl());
    }
}
