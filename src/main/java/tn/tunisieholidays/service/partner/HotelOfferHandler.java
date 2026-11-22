package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.HotelOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.dto.OfferDtos.HotelOfferRequest;
import tn.tunisieholidays.dto.OfferDtos.HotelOfferView;
import tn.tunisieholidays.repository.HotelOfferRepository;
import tn.tunisieholidays.repository.HotelRepository;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HotelOfferHandler implements OfferHandler<HotelOffer, HotelOfferRequest> {

    private final HotelOfferRepository repository;
    private final HotelRepository hotelRepository;

    public OfferType type() {
        return OfferType.HOTEL;
    }

    public Set<PartnerType> allowedPartnerTypes() {
        return Set.of(PartnerType.AGENCY, PartnerType.HOTEL);
    }

    public Class<HotelOfferRequest> requestType() {
        return HotelOfferRequest.class;
    }

    public JpaRepository<HotelOffer, Long> repository() {
        return repository;
    }

    public List<HotelOffer> findByPartner(Long partnerId) {
        return repository.findByPartnerId(partnerId);
    }

    public HotelOffer newEntity() {
        return new HotelOffer();
    }

    public void apply(HotelOffer e, HotelOfferRequest r) {
        e.setHotel(hotelRepository.findById(r.hotelId())
                .orElseThrow(() -> ApiException.badRequest("Hôtel introuvable")));
        e.setBoard(r.board());
        e.setRoomType(r.roomType());
        e.setMaxGuests(r.maxGuests());
        e.setFreeCancellation(r.freeCancellation());
    }

    public Object toView(HotelOffer e, long clicks) {
        return new HotelOfferView(e.getId(), e.isActive(), clicks, e.getHotel().getId(), e.getHotel().getName(),
                e.getHotel().getDestination().getName(), e.getBoard(), e.getRoomType(), e.getMaxGuests(),
                e.isFreeCancellation(), e.getPrice(), e.getCurrency(), e.getDeeplinkUrl());
    }
}
