package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.CarCategory;
import tn.tunisieholidays.domain.CarOffer;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.domain.Transmission;
import tn.tunisieholidays.dto.SearchDtos.CarResult;
import tn.tunisieholidays.dto.SearchDtos.PickupLocation;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.repository.CarOfferRepository;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarSearchService {

    private final CarOfferRepository repository;
    private final CurrencyService currencyService;

    public record Criteria(String pickupCode, LocalDateTime pickupAt, LocalDateTime dropoffAt, int driverAge,
                           String currency, Set<CarCategory> categories, Transmission transmission,
                           boolean freeCancellationOnly, BigDecimal maxPricePerDay) {
    }

    @Transactional(readOnly = true)
    public List<PickupLocation> locations() {
        return repository.findPickupLocations(PartnerStatus.ACTIVE).stream()
                .map(row -> new PickupLocation((String) row[0], (String) row[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public SearchResponse<CarResult> search(Criteria c) {
        if (c.pickupCode() == null || c.pickupCode().isBlank()) {
            throw ApiException.badRequest("Le lieu de prise en charge est obligatoire");
        }
        if (c.driverAge() < 18 || c.driverAge() > 99) {
            throw ApiException.badRequest("Âge du conducteur invalide");
        }
        int days = Stays.rentalDays(c.pickupAt(), c.dropoffAt());
        Converter converter = currencyService.converter(c.currency());
        List<CarResult> results = repository.findActiveByPickup(c.pickupCode().trim(), PartnerStatus.ACTIVE).stream()
                .filter(o -> o.getMinDriverAge() <= c.driverAge())
                .filter(o -> c.categories() == null || c.categories().isEmpty() || c.categories().contains(o.getCategory()))
                .filter(o -> c.transmission() == null || c.transmission() == o.getTransmission())
                .filter(o -> !c.freeCancellationOnly() || o.isFreeCancellation())
                .map(o -> toResult(o, days, converter))
                .filter(r -> c.maxPricePerDay() == null || r.pricePerDay().compareTo(c.maxPricePerDay()) <= 0)
                .sorted(Comparator.comparing(CarResult::totalPrice))
                .toList();
        return new SearchResponse<>(results, results.size(), converter.target(), null, days, List.of("PARTNERS"));
    }

    private CarResult toResult(CarOffer o, int days, Converter converter) {
        BigDecimal perDay = converter.convert(o.getPrice(), o.getCurrency());
        return new CarResult(o.getId(), o.getPartner().getName(), o.getCategory(), o.getModel(), o.getSeats(),
                o.getDoors(), o.getBags(), o.getTransmission(), o.isAirConditioning(), o.isUnlimitedMileage(),
                o.isFreeCancellation(), o.getMinDriverAge(), o.getImageUrl(), o.getPickupLabel(), perDay,
                perDay.multiply(BigDecimal.valueOf(days)), converter.target(), "/api/go/CAR/" + o.getId());
    }
}
