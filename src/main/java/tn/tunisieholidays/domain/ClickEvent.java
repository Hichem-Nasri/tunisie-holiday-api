package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferType offerType;

    private Long offerId;

    private Long partnerId;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(length = 64)
    private String visitorHash;

    @Column(length = 300)
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
