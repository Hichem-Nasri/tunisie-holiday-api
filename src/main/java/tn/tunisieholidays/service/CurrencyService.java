package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.ExchangeRate;
import tn.tunisieholidays.dto.CatalogDtos.CurrencyView;
import tn.tunisieholidays.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    public static final String DEFAULT_CURRENCY = "TND";

    private final ExchangeRateRepository repository;

    @Transactional(readOnly = true)
    public List<CurrencyView> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(r -> r.getCurrency().equals(DEFAULT_CURRENCY) ? "" : r.getCurrency()))
                .map(CurrencyView::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public Converter converter(String targetCurrency) {
        Map<String, BigDecimal> rates = repository.findAll().stream()
                .collect(Collectors.toMap(ExchangeRate::getCurrency, ExchangeRate::getRateToTnd));
        String target = targetCurrency == null || targetCurrency.isBlank()
                ? DEFAULT_CURRENCY
                : targetCurrency.trim().toUpperCase(Locale.ROOT);
        if (!rates.containsKey(target)) {
            throw ApiException.badRequest("Devise non prise en charge : " + target);
        }
        return new Converter(rates, target);
    }

    @Transactional
    public CurrencyView updateRate(String currency, BigDecimal rateToTnd) {
        String code = currency.toUpperCase(Locale.ROOT);
        if (DEFAULT_CURRENCY.equals(code)) {
            throw ApiException.badRequest("Le taux du dinar tunisien est fixé à 1");
        }
        ExchangeRate rate = repository.findById(code)
                .orElseThrow(() -> ApiException.notFound("Devise introuvable : " + code));
        rate.setRateToTnd(rateToTnd);
        rate.setUpdatedAt(Instant.now());
        return CurrencyView.of(repository.save(rate));
    }

    public record Converter(Map<String, BigDecimal> rates, String target) {

        public BigDecimal convert(BigDecimal amount, String from) {
            if (amount == null) {
                return null;
            }
            String source = from == null ? DEFAULT_CURRENCY : from.toUpperCase(Locale.ROOT);
            if (source.equals(target)) {
                return amount.setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal sourceRate = rates.get(source);
            if (sourceRate == null) {
                throw ApiException.badRequest("Devise non prise en charge : " + source);
            }
            BigDecimal inTnd = amount.multiply(sourceRate);
            return inTnd.divide(rates.get(target), 2, RoundingMode.HALF_UP);
        }
    }
}
