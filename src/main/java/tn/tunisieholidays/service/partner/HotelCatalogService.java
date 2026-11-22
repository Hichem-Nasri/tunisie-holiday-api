package tn.tunisieholidays.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Hotel;
import tn.tunisieholidays.dto.OfferDtos.HotelRequest;
import tn.tunisieholidays.dto.OfferDtos.HotelView;
import tn.tunisieholidays.repository.HotelRepository;
import tn.tunisieholidays.service.DestinationService;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelCatalogService {

    private final HotelRepository repository;
    private final DestinationService destinationService;

    @Transactional(readOnly = true)
    public List<HotelView> search(String query) {
        List<Hotel> hotels = query == null || query.isBlank()
                ? repository.findAllWithDestination()
                : repository.search(query.trim());
        return hotels.stream().map(HotelView::of).toList();
    }

    @Transactional
    public HotelView create(HotelRequest request) {
        Hotel hotel = new Hotel();
        apply(hotel, request);
        return HotelView.of(repository.save(hotel));
    }

    @Transactional
    public HotelView update(Long id, HotelRequest request) {
        Hotel hotel = repository.findById(id).orElseThrow(() -> ApiException.notFound("Hôtel introuvable"));
        apply(hotel, request);
        return HotelView.of(repository.save(hotel));
    }

    private void apply(Hotel h, HotelRequest r) {
        h.setName(r.name());
        h.setDestination(destinationService.require(r.destinationId()));
        h.setType(r.type());
        h.setStars(r.stars());
        h.setAddress(r.address());
        h.setLocationLabel(r.locationLabel());
        h.setDescription(r.description());
        h.setImageUrl(r.imageUrl());
        h.setLatitude(r.latitude());
        h.setLongitude(r.longitude());
        h.getAmenities().clear();
        if (r.amenities() != null) {
            h.getAmenities().addAll(new HashSet<>(r.amenities()));
        }
    }
}
