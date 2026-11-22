package tn.tunisieholidays.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.ActivityCategory;
import tn.tunisieholidays.domain.ActivityOffer;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.SearchDtos.ActivityResult;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.repository.ActivityOfferRepository;
import tn.tunisieholidays.service.CurrencyService;
import tn.tunisieholidays.service.CurrencyService.Converter;
import tn.tunisieholidays.service.DestinationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivitySearchService {

    private final ActivityOfferRepository repository;
    private final DestinationService destinationService;
    private final CurrencyService currencyService;

    public record Criteria(String query, LocalDate date, int participants, String currency,
                           Set<ActivityCategory> categories, BigDecimal maxPrice) {
    }

    @Transactional(readOnly = true)
    public SearchResponse<ActivityResult> search(Criteria c) {
        if (c.date() != null && c.date().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("La date de l'activité est passée");
        }
        if (c.participants() < 1 || c.participants() > 30) {
            throw ApiException.badRequest("Nombre de participants invalide");
        }
        Converter converter = currencyService.converter(c.currency());
        List<Long> destinationIds = destinationService.resolveIds(c.query());
        if (destinationIds.isEmpty()) {
            return new SearchResponse<>(List.of(), 0, converter.target(), null, null, List.of("PARTNERS"));
        }
        List<ActivityResult> results = repository.findActiveByDestinations(destinationIds, PartnerStatus.ACTIVE).stream()
                .filter(o -> c.categories() == null || c.categories().isEmpty() || c.categories().contains(o.getCategory()))
                .map(o -> toResult(o, c.participants(), converter))
                .filter(r -> c.maxPrice() == null || r.pricePerPerson().compareTo(c.maxPrice()) <= 0)
                .sorted(Comparator.comparing(ActivityResult::pricePerPerson))
                .toList();
        return new SearchResponse<>(results, results.size(), converter.target(), null, null, List.of("PARTNERS"));
    }

    private ActivityResult toResult(ActivityOffer o, int participants, Converter converter) {
        BigDecimal perPerson = converter.convert(o.getPrice(), o.getCurrency());
        return new ActivityResult(o.getId(), o.getPartner().getName(), o.getTitle(), o.getCategory(),
                o.getDestination().getName(), o.getDurationHours(), o.getMeetingPoint(), o.getDescription(),
                o.getImageUrl(), perPerson, perPerson.multiply(BigDecimal.valueOf(participants)),
                converter.target(), "/api/go/ACTIVITY/" + o.getId());
    }
}
