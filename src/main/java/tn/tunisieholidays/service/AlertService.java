package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.PriceAlert;
import tn.tunisieholidays.dto.CatalogDtos.PriceAlertRequest;
import tn.tunisieholidays.repository.PriceAlertRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final PriceAlertRepository repository;

    @Transactional
    public Long create(PriceAlertRequest request) {
        if (request.returnDate() != null && request.returnDate().isBefore(request.departureDate())) {
            throw ApiException.badRequest("La date de retour doit suivre la date de départ");
        }
        PriceAlert alert = new PriceAlert();
        alert.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        alert.setOriginCode(request.origin());
        alert.setDestinationCode(request.destination());
        alert.setDepartureDate(request.departureDate());
        alert.setReturnDate(request.returnDate());
        alert.setMaxPrice(request.maxPrice());
        alert.setCurrency(request.currency());
        return repository.save(alert).getId();
    }
}
