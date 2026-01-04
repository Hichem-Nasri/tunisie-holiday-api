package tn.tunisieconnect.reservation.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record HostRevenueResponse(
        BigDecimal totalRevenue,
        BigDecimal monthlyRevenue,
        Long confirmedReservations
) {}