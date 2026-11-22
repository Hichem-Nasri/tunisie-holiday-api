package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "flight_offers")
public class FlightOffer extends BaseOffer {

    @Column(nullable = false, length = 3)
    private String airlineCode;

    @Column(nullable = false, length = 100)
    private String airlineName;

    @Column(length = 10)
    private String flightNumber;

    @Column(nullable = false, length = 3)
    private String originCode;

    @Column(nullable = false, length = 3)
    private String destinationCode;

    @Column(nullable = false)
    private LocalDateTime departureAt;

    @Column(nullable = false)
    private LocalDateTime arrivalAt;

    @Column(nullable = false)
    private int stops;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Cabin cabin;

    @Column(nullable = false)
    private boolean baggageIncluded;

    @Column(nullable = false)
    private int seatsAvailable;
}
