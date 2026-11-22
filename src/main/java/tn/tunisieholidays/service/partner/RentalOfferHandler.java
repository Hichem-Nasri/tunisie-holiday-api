package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.domain.RentalOffer;
import tn.tunisieholidays.dto.OfferDtos.RentalOfferRequest;
import tn.tunisieholidays.dto.OfferDtos.RentalOfferView;
import tn.tunisieholidays.repository.RentalOfferRepository;
import tn.tunisieholidays.service.DestinationService;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RentalOfferHandler implements OfferHandler<RentalOffer, RentalOfferRequest> {

    private final RentalOfferRepository repository;
    private final DestinationService destinationService;

    public OfferType type() {
        return OfferType.RENTAL;
    }

    public Set<PartnerType> allowedPartnerTypes() {
        return Set.of(PartnerType.HOST, PartnerType.AGENCY);
    }

    public Class<RentalOfferRequest> requestType() {
        return RentalOfferRequest.class;
    }

    public JpaRepository<RentalOffer, Long> repository() {
        return repository;
    }

    public List<RentalOffer> findByPartner(Long partnerId) {
        return repository.findByPartnerId(partnerId);
    }

    public RentalOffer newEntity() {
        return new RentalOffer();
    }

    public void apply(RentalOffer e, RentalOfferRequest r) {
        e.setDestination(destinationService.require(r.destinationId()));
        e.setTitle(r.title());
        e.setType(r.type());
        e.setCapacity(r.capacity());
        e.setBedrooms(r.bedrooms());
        e.setBathrooms(r.bathrooms());
        e.setPool(r.pool());
        e.setWifi(r.wifi());
        e.setSeaView(r.seaView());
        e.setMinNights(r.minNights());
        e.setDescription(r.description());
        e.setImageUrl(r.imageUrl());
    }

    public Object toView(RentalOffer e, long clicks) {
        return new RentalOfferView(e.getId(), e.isActive(), clicks, e.getDestination().getId(),
                e.getDestination().getName(), e.getTitle(), e.getType(), e.getCapacity(), e.getBedrooms(),
                e.getBathrooms(), e.isPool(), e.isWifi(), e.isSeaView(), e.getMinNights(), e.getDescription(),
                e.getImageUrl(), e.getPrice(), e.getCurrency(), e.getDeeplinkUrl());
    }
}
