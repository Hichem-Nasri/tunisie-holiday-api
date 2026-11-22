package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Board;
import tn.tunisieholidays.domain.Hotel;
import tn.tunisieholidays.domain.HotelOffer;
import tn.tunisieholidays.domain.HotelType;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.SearchDtos.HotelOption;
import tn.tunisieholidays.dto.SearchDtos.HotelResult;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.repository.HotelOfferRepository;
import tn.tunisieholidays.repository.HotelRepository;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;
import tn.tunisieholidays.service.DestinationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelSearchService {

    public enum Sort { RECOMMENDED, PRICE, RATING, STARS }

    private final HotelOfferRepository offerRepository;
    private final HotelRepository hotelRepository;
    private final DestinationService destinationService;
    private final CurrencyService currencyService;

    public record Criteria(String query, LocalDate checkIn, LocalDate checkOut, int adults, int children, int rooms,
                           String currency, Set<Integer> stars, Set<Board> boards, Set<HotelType> types,
                           Set<String> amenities, BigDecimal maxPricePerNight, boolean freeCancellationOnly,
                           Long hotelId, Sort sort) {
    }

    @Transactional(readOnly = true)
    public SearchResponse<HotelResult> search(Criteria c) {
        int nights = Stays.nights(c.checkIn(), c.checkOut());
        if (c.adults() < 1 || c.rooms() < 1 || c.rooms() > 9 || c.children() < 0) {
            throw ApiException.badRequest("Nombre de voyageurs ou de chambres invalide");
        }
        Converter converter = currencyService.converter(c.currency());
        List<HotelOffer> offers = loadOffers(c);
        int guestsPerRoom = (int) Math.ceil((double) (c.adults() + c.children()) / c.rooms());

        Map<Hotel, List<HotelOffer>> byHotel = new LinkedHashMap<>();
        for (HotelOffer o : offers) {
            if (o.getMaxGuests() < guestsPerRoom) {
                continue;
            }
            if (c.boards() != null && !c.boards().isEmpty() && !c.boards().contains(o.getBoard())) {
                continue;
            }
            if (c.freeCancellationOnly() && !o.isFreeCancellation()) {
                continue;
            }
            byHotel.computeIfAbsent(o.getHotel(), h -> new ArrayList<>()).add(o);
        }

        List<HotelResult> results = new ArrayList<>();
        for (Map.Entry<Hotel, List<HotelOffer>> e : byHotel.entrySet()) {
            Hotel h = e.getKey();
            if (c.stars() != null && !c.stars().isEmpty() && !c.stars().contains(h.getStars())) {
                continue;
            }
            if (c.types() != null && !c.types().isEmpty() && !c.types().contains(h.getType())) {
                continue;
            }
            if (c.amenities() != null && !c.amenities().isEmpty() && !h.getAmenities().containsAll(c.amenities())) {
                continue;
            }
            List<HotelOption> options = e.getValue().stream()
                    .map(o -> {
                        BigDecimal perNight = converter.convert(o.getPrice(), o.getCurrency());
                        BigDecimal total = perNight.multiply(BigDecimal.valueOf((long) nights * c.rooms()));
                        return new HotelOption(o.getId(), o.getPartner().getName(), o.getBoard(), o.getRoomType(),
                                perNight, total, converter.target(), o.isFreeCancellation(),
                                "/api/go/HOTEL/" + o.getId());
                    })
                    .sorted(Comparator.comparing(HotelOption::totalPrice))
                    .toList();
            HotelOption best = options.get(0);
            if (c.maxPricePerNight() != null && best.pricePerNight().compareTo(c.maxPricePerNight()) > 0) {
                continue;
            }
            results.add(new HotelResult(h.getId(), h.getName(), h.getType(), h.getStars(), h.getDestination().getName(),
                    h.getDestination().getSlug(), h.getLocationLabel(), h.getImageUrl(), h.getRating(),
                    h.getReviewCount(), Set.copyOf(h.getAmenities()), h.getLatitude(), h.getLongitude(), options,
                    best.totalPrice(), best.pricePerNight(), converter.target()));
        }
        results.sort(comparator(c.sort() == null ? Sort.RECOMMENDED : c.sort()));
        return new SearchResponse<>(results, results.size(), converter.target(), nights, null, List.of("PARTNERS"));
    }

    private List<HotelOffer> loadOffers(Criteria c) {
        if (c.hotelId() != null) {
            return offerRepository.findActiveByHotel(c.hotelId(), PartnerStatus.ACTIVE);
        }
        List<Long> destinationIds = destinationService.resolveIds(c.query());
        if (!destinationIds.isEmpty()) {
            return offerRepository.findActiveByDestinations(destinationIds, PartnerStatus.ACTIVE);
        }
        List<Hotel> hotels = hotelRepository.search(c.query() == null ? "" : c.query().trim());
        if (hotels.isEmpty()) {
            return List.of();
        }
        Set<Long> hotelIds = hotels.stream().map(Hotel::getId).collect(Collectors.toSet());
        List<Long> ids = hotels.stream().map(h -> h.getDestination().getId()).distinct().toList();
        return offerRepository.findActiveByDestinations(ids, PartnerStatus.ACTIVE).stream()
                .filter(o -> hotelIds.contains(o.getHotel().getId()))
                .toList();
    }

    private Comparator<HotelResult> comparator(Sort sort) {
        Comparator<HotelResult> byPrice = Comparator.comparing(HotelResult::bestTotal);
        Comparator<HotelResult> byRating = Comparator.comparing(
                (HotelResult r) -> r.rating() == null ? BigDecimal.ZERO : r.rating()).reversed();
        return switch (sort) {
            case PRICE -> byPrice;
            case RATING -> byRating.thenComparing(byPrice);
            case STARS -> Comparator.comparingInt(HotelResult::stars).reversed().thenComparing(byPrice);
            case RECOMMENDED -> Comparator.comparingDouble((HotelResult r) ->
                    -(r.rating() == null ? 7.0 : r.rating().doubleValue()) * 10
                            + r.bestPerNight().doubleValue() / 50.0);
        };
    }
}
