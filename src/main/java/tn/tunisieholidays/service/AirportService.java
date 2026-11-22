package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Airport;
import tn.tunisieholidays.dto.CatalogDtos.AirportView;
import tn.tunisieholidays.repository.AirportRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository repository;

    @Transactional(readOnly = true)
    public List<AirportView> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return repository.findAll().stream()
                    .filter(a -> "TN".equals(a.getCountryCode()))
                    .map(AirportView::of)
                    .toList();
        }
        return repository.search(query.trim()).stream().limit(10).map(AirportView::of).toList();
    }

    @Transactional(readOnly = true)
    public Set<String> expand(String code) {
        if (code == null || !code.matches("^[A-Za-z]{3}$")) {
            throw ApiException.badRequest("Code aéroport ou ville invalide : " + code);
        }
        String upper = code.toUpperCase(Locale.ROOT);
        Set<String> codes = new LinkedHashSet<>();
        repository.findByCityCode(upper).stream().map(Airport::getCode).forEach(codes::add);
        if (codes.isEmpty()) {
            codes.add(upper);
        }
        return codes;
    }
}
