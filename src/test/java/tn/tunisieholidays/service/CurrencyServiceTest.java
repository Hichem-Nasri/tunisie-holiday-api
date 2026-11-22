package tn.tunisieholidays.service;

import org.junit.jupiter.api.Test;
import tn.tunisieholidays.common.ApiException;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyServiceTest {

    private final CurrencyService.Converter toEur = new CurrencyService.Converter(
            Map.of("TND", BigDecimal.ONE, "EUR", new BigDecimal("3.40"), "USD", new BigDecimal("3.10")), "EUR");

    @Test
    void convertsTndToEur() {
        assertThat(toEur.convert(new BigDecimal("340"), "TND")).isEqualByComparingTo("100.00");
    }

    @Test
    void keepsSameCurrency() {
        assertThat(toEur.convert(new BigDecimal("12.345"), "EUR")).isEqualByComparingTo("12.35");
    }

    @Test
    void convertsBetweenForeignCurrencies() {
        assertThat(toEur.convert(new BigDecimal("100"), "USD")).isEqualByComparingTo("91.18");
    }

    @Test
    void rejectsUnknownCurrency() {
        assertThatThrownBy(() -> toEur.convert(BigDecimal.TEN, "JPY")).isInstanceOf(ApiException.class);
    }
}
