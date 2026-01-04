package tn.tunisieconnect.reservation.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MonthlyRevenueResponse(
        int month,
        BigDecimal revenue
) {}