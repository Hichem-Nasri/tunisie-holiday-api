package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import tn.tunisieholidays.common.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseOffer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 1000)
    private String deeplinkUrl;

    @Column(nullable = false)
    private boolean active = true;
}
