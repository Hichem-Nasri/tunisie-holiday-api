package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "car_offers")
public class CarOffer extends BaseOffer {

    @Column(nullable = false, length = 20)
    private String pickupCode;

    @Column(nullable = false, length = 150)
    private String pickupLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarCategory category;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private int seats;

    @Column(nullable = false)
    private int doors;

    @Column(nullable = false)
    private int bags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Transmission transmission;

    @Column(nullable = false)
    private boolean airConditioning;

    @Column(nullable = false)
    private boolean unlimitedMileage;

    @Column(nullable = false)
    private boolean freeCancellation;

    @Column(nullable = false)
    private int minDriverAge;

    @Column(length = 500)
    private String imageUrl;
}
