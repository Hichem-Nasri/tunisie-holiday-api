package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.domain.ActivityCategory;
import tn.tunisieholidays.domain.Board;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.CatalogDtos.DealView;
import tn.tunisieholidays.repository.ActivityOfferRepository;
import tn.tunisieholidays.repository.CarOfferRepository;
import tn.tunisieholidays.repository.HotelOfferRepository;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {

    private final HotelOfferRepository hotelOfferRepository;
    private final CarOfferRepository carOfferRepository;
    private final ActivityOfferRepository activityOfferRepository;
    private final CurrencyService currencyService;

    @Transactional(readOnly = true)
    public List<DealView> deals(String currency) {
        Converter converter = currencyService.converter(currency);
        List<DealView> deals = new ArrayList<>();
        var hotels = hotelOfferRepository.findActiveOrderByPrice(PartnerStatus.ACTIVE);
        hotels.stream()
                .filter(o -> o.getBoard() == Board.ALL_INCLUSIVE)
                .findFirst()
                .or(() -> hotels.stream().findFirst())
                .ifPresent(o -> deals.add(new DealView(OfferType.HOTEL,
                        o.getHotel().getName() + ", " + o.getHotel().getDestination().getName(),
                        o.getRoomType() + " · " + o.getPartner().getName(),
                        "Hôtel · " + boardLabel(o.getBoard()),
                        converter.convert(o.getPrice(), o.getCurrency()), converter.target(), "nuit",
                        o.getHotel().getImageUrl(), "/hotels?q=" + o.getHotel().getDestination().getSlug())));
        carOfferRepository.findActiveOrderByPrice(PartnerStatus.ACTIVE).stream().findFirst()
                .ifPresent(o -> deals.add(new DealView(OfferType.CAR,
                        o.getModel() + " à " + o.getPickupLabel(),
                        (o.isUnlimitedMileage() ? "Kilométrage illimité" : "Kilométrage limité")
                                + (o.isFreeCancellation() ? " · annulation gratuite" : ""),
                        "Voiture · " + o.getPartner().getName(),
                        converter.convert(o.getPrice(), o.getCurrency()), converter.target(), "jour",
                        o.getImageUrl(), "/voitures?location=" + o.getPickupCode())));
        var activities = activityOfferRepository.findAllActive(PartnerStatus.ACTIVE);
        activities.stream()
                .filter(o -> o.getCategory() == ActivityCategory.DESERT)
                .findFirst()
                .or(() -> activities.stream().findFirst())
                .ifPresent(o -> deals.add(new DealView(OfferType.ACTIVITY, o.getTitle(),
                        o.getDestination().getName() + " · " + o.getDurationHours().stripTrailingZeros().toPlainString() + " h",
                        "Loisirs · " + o.getDestination().getName(),
                        converter.convert(o.getPrice(), o.getCurrency()), converter.target(), "personne",
                        o.getImageUrl(), "/loisirs?q=" + o.getDestination().getSlug())));
        return deals;
    }

    private String boardLabel(Board board) {
        return switch (board) {
            case ROOM_ONLY -> "Logement seul";
            case BREAKFAST -> "Petit-déjeuner";
            case HALF_BOARD -> "Demi-pension";
            case FULL_BOARD -> "Pension complète";
            case ALL_INCLUSIVE -> "All inclusive";
        };
    }
}
