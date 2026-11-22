package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_offers")
public class RentalOffer extends BaseOffer {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false, length = 180)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalType type;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int bedrooms;

    @Column(nullable = false)
    private int bathrooms;

    @Column(nullable = false)
    private boolean pool;

    @Column(nullable = false)
    private boolean wifi;

    @Column(nullable = false)
    private boolean seaView;

    @Column(nullable = false)
    private int minNights;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String imageUrl;
}
