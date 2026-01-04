package tn.tunisieconnect.listing.dto;

import lombok.Builder;
import tn.tunisieconnect.listing.entity.ListingStatus;
import tn.tunisieconnect.listing.entity.ListingType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ListingResponse(
        Long id,
        String title,
        String description,
        String city,
        String address,
        BigDecimal pricePerNight,
        Integer maxGuests,
        ListingType type,
        ListingStatus status,
        List<String> images,
        Integer bedrooms,
        Integer beds,
        Integer bathrooms,
        List<String> amenities,
        String houseRules,
        LocalTime checkInTime,
        LocalTime checkOutTime
) {
}
