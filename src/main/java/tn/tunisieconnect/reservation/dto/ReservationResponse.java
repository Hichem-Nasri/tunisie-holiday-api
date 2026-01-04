package tn.tunisieconnect.reservation.dto;

import lombok.Builder;
import tn.tunisieconnect.reservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ReservationResponse(
        Long id,
        Long listingId,
        String listingTitle,
        String city,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer guests,
        BigDecimal totalPrice,
        ReservationStatus status,
        LocalDateTime createdAt
) {
}
