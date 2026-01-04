package tn.tunisieconnect.listing.dto;

import tn.tunisieconnect.listing.entity.ListingType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record ListingRequest(

        // Base
        String title,
        String description,
        String city,
        String address,
        BigDecimal pricePerNight,
        Integer maxGuests,
        ListingType type,

        // Configuration
        Integer bedrooms,
        Integer beds,
        Integer bathrooms,

        // Équipements
        List<String> amenities,

        // Règles
        String houseRules,

        // Check-in / Check-out
        LocalTime checkInTime,
        LocalTime checkOutTime
) {
}
