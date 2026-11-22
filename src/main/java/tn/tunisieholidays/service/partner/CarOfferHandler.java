package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.domain.CarOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.dto.OfferDtos.CarOfferRequest;
import tn.tunisieholidays.dto.OfferDtos.CarOfferView;
import tn.tunisieholidays.repository.CarOfferRepository;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CarOfferHandler implements OfferHandler<CarOffer, CarOfferRequest> {

    private final CarOfferRepository repository;

    public OfferType type() {
        return OfferType.CAR;
    }

    public Set<PartnerType> allowedPartnerTypes() {
        return Set.of(PartnerType.CAR_RENTAL, PartnerType.AGENCY);
    }

    public Class<CarOfferRequest> requestType() {
        return CarOfferRequest.class;
    }

    public JpaRepository<CarOffer, Long> repository() {
        return repository;
    }

    public List<CarOffer> findByPartner(Long partnerId) {
        return repository.findByPartnerIdOrderByCreatedAtDesc(partnerId);
    }

    public CarOffer newEntity() {
        return new CarOffer();
    }

    public void apply(CarOffer e, CarOfferRequest r) {
        e.setPickupCode(r.pickupCode());
        e.setPickupLabel(r.pickupLabel());
        e.setCategory(r.category());
        e.setModel(r.model());
        e.setSeats(r.seats());
        e.setDoors(r.doors());
        e.setBags(r.bags());
        e.setTransmission(r.transmission());
        e.setAirConditioning(r.airConditioning());
        e.setUnlimitedMileage(r.unlimitedMileage());
        e.setFreeCancellation(r.freeCancellation());
        e.setMinDriverAge(r.minDriverAge());
        e.setImageUrl(r.imageUrl());
    }

    public Object toView(CarOffer e, long clicks) {
        return new CarOfferView(e.getId(), e.isActive(), clicks, e.getPickupCode(), e.getPickupLabel(),
                e.getCategory(), e.getModel(), e.getSeats(), e.getDoors(), e.getBags(), e.getTransmission(),
                e.isAirConditioning(), e.isUnlimitedMileage(), e.isFreeCancellation(), e.getMinDriverAge(),
                e.getImageUrl(), e.getPrice(), e.getCurrency(), e.getDeeplinkUrl());
    }
}
