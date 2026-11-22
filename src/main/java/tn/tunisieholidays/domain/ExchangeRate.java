package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @Column(length = 3)
    private String currency;

    @Column(nullable = false, length = 60)
    private String label;

    @Column(nullable = false, length = 8)
    private String symbol;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal rateToTnd;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
