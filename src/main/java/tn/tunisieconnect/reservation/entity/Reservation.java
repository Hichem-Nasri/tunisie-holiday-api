package tn.tunisieconnect.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.tunisieconnect.listing.entity.Listing;
import tn.tunisieconnect.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 relations
    @ManyToOne(fetch = FetchType.LAZY)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    private User guest; // nullable pour l’instant (seed)

    // 📅 dates
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // 👥
    private Integer guests;

    // 💰
    private BigDecimal totalPrice;

    // 📌 statut
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
