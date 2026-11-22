package tn.tunisieholidays.dto;

import tn.tunisieholidays.domain.ActivityCategory;
import tn.tunisieholidays.domain.Board;
import tn.tunisieholidays.domain.CarCategory;
import tn.tunisieholidays.domain.HotelType;
import tn.tunisieholidays.domain.RentalType;
import tn.tunisieholidays.domain.Transmission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class SearchDtos {

    private SearchDtos() {
    }

    public record SearchResponse<T>(List<T> results, int total, String currency, Integer nights, Integer days,
                                    List<String> sources) {
    }

    public record FlightLeg(String originCode, String destinationCode, LocalDateTime departureAt,
                            LocalDateTime arrivalAt, long durationMinutes, int stops, String flightNumber) {
    }

    public record FlightOption(String offerKey, String source, String partnerName, BigDecimal price, String currency,
                               boolean baggageIncluded, String redirectUrl) {
    }

    public record FlightResult(String id, String airlineCode, String airlineName, FlightLeg outbound,
                               FlightLeg inbound, List<FlightOption> offers, BigDecimal minPrice, String currency,
                               long totalDurationMinutes, int maxStops) {
    }

    public record HotelOption(Long offerId, String partnerName, Board board, String roomType, BigDecimal pricePerNight,
                              BigDecimal totalPrice, String currency, boolean freeCancellation, String redirectUrl) {
    }

    public record HotelResult(Long hotelId, String name, HotelType type, int stars, String destination,
                              String destinationSlug, String locationLabel, String imageUrl, BigDecimal rating,
                              int reviewCount, Set<String> amenities, Double latitude, Double longitude,
                              List<HotelOption> offers, BigDecimal bestTotal, BigDecimal bestPerNight,
                              String currency) {
    }

    public record CarResult(Long offerId, String partnerName, CarCategory category, String model, int seats,
                            int doors, int bags, Transmission transmission, boolean airConditioning,
                            boolean unlimitedMileage, boolean freeCancellation, int minDriverAge, String imageUrl,
                            String pickupLabel, BigDecimal pricePerDay, BigDecimal totalPrice, String currency,
                            String redirectUrl) {
    }

    public record PickupLocation(String code, String label) {
    }

    public record RentalResult(Long offerId, String partnerName, String title, RentalType type, String destination,
                               int capacity, int bedrooms, int bathrooms, boolean pool, boolean wifi,
                               boolean seaView, int minNights, String description, String imageUrl,
                               BigDecimal pricePerNight, BigDecimal totalPrice, String currency, String redirectUrl) {
    }

    public record ActivityResult(Long offerId, String partnerName, String title, ActivityCategory category,
                                 String destination, BigDecimal durationHours, String meetingPoint,
                                 String description, String imageUrl, BigDecimal pricePerPerson,
                                 BigDecimal totalPrice, String currency, String redirectUrl) {
    }
}
