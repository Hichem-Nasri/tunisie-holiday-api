package tn.tunisieconnect.reservation.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TopListingRevenueResponse(
        Long listingId,
        String title,
        String city,
        BigDecimal revenue,
        Long reservationsCount
) {}
