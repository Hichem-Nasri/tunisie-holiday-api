package tn.tunisieholidays.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tn.tunisieholidays.domain.ActivityCategory;
import tn.tunisieholidays.domain.Board;
import tn.tunisieholidays.domain.Cabin;
import tn.tunisieholidays.domain.CarCategory;
import tn.tunisieholidays.domain.Hotel;
import tn.tunisieholidays.domain.HotelType;
import tn.tunisieholidays.domain.RentalType;
import tn.tunisieholidays.domain.Transmission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public final class OfferDtos {

    public static final String URL_PATTERN = "^https?://\\S+$";
    public static final String CURRENCY_PATTERN = "^(TND|EUR|USD|GBP)$";
    public static final String IATA_PATTERN = "^[A-Z]{3}$";

    private OfferDtos() {
    }

    public interface OfferRequest {
        BigDecimal price();

        String currency();

        String deeplinkUrl();
    }

    public record FlightOfferRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{2,3}$") String airlineCode,
            @NotBlank @Size(max = 100) String airlineName,
            @Size(max = 10) String flightNumber,
            @NotBlank @Pattern(regexp = IATA_PATTERN) String originCode,
            @NotBlank @Pattern(regexp = IATA_PATTERN) String destinationCode,
            @NotNull LocalDateTime departureAt,
            @NotNull LocalDateTime arrivalAt,
            @Min(0) @Max(3) int stops,
            @NotNull Cabin cabin,
            boolean baggageIncluded,
            @Min(0) @Max(999) int seatsAvailable,
            @NotNull @Positive BigDecimal price,
            @NotBlank @Pattern(regexp = CURRENCY_PATTERN) String currency,
            @NotBlank @Size(max = 1000) @Pattern(regexp = URL_PATTERN) String deeplinkUrl) implements OfferRequest {
    }

    public record FlightOfferView(Long id, boolean active, long clicks30d, String airlineCode, String airlineName,
                                  String flightNumber, String originCode, String destinationCode,
                                  LocalDateTime departureAt, LocalDateTime arrivalAt, int stops, Cabin cabin,
                                  boolean baggageIncluded, int seatsAvailable, BigDecimal price, String currency,
                                  String deeplinkUrl) {
    }

    public record HotelOfferRequest(
            @NotNull Long hotelId,
            @NotNull Board board,
            @NotBlank @Size(max = 120) String roomType,
            @Min(1) @Max(20) int maxGuests,
            boolean freeCancellation,
            @NotNull @Positive BigDecimal price,
            @NotBlank @Pattern(regexp = CURRENCY_PATTERN) String currency,
            @NotBlank @Size(max = 1000) @Pattern(regexp = URL_PATTERN) String deeplinkUrl) implements OfferRequest {
    }

    public record HotelOfferView(Long id, boolean active, long clicks30d, Long hotelId, String hotelName,
                                 String destination, Board board, String roomType, int maxGuests,
                                 boolean freeCancellation, BigDecimal price, String currency, String deeplinkUrl) {
    }

    public record CarOfferRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9-]{2,20}$") String pickupCode,
            @NotBlank @Size(max = 150) String pickupLabel,
            @NotNull CarCategory category,
            @NotBlank @Size(max = 100) String model,
            @Min(1) @Max(9) int seats,
            @Min(2) @Max(5) int doors,
            @Min(0) @Max(10) int bags,
            @NotNull Transmission transmission,
            boolean airConditioning,
            boolean unlimitedMileage,
            boolean freeCancellation,
            @Min(18) @Max(30) int minDriverAge,
            @Size(max = 500) String imageUrl,
            @NotNull @Positive BigDecimal price,
            @NotBlank @Pattern(regexp = CURRENCY_PATTERN) String currency,
            @NotBlank @Size(max = 1000) @Pattern(regexp = URL_PATTERN) String deeplinkUrl) implements OfferRequest {
    }

    public record CarOfferView(Long id, boolean active, long clicks30d, String pickupCode, String pickupLabel,
                               CarCategory category, String model, int seats, int doors, int bags,
                               Transmission transmission, boolean airConditioning, boolean unlimitedMileage,
                               boolean freeCancellation, int minDriverAge, String imageUrl, BigDecimal price,
                               String currency, String deeplinkUrl) {
    }

    public record RentalOfferRequest(
            @NotNull Long destinationId,
            @NotBlank @Size(max = 180) String title,
            @NotNull RentalType type,
            @Min(1) @Max(30) int capacity,
            @Min(0) @Max(15) int bedrooms,
            @Min(0) @Max(15) int bathrooms,
            boolean pool,
            boolean wifi,
            boolean seaView,
            @Min(1) @Max(60) int minNights,
            @Size(max = 4000) String description,
            @Size(max = 500) String imageUrl,
            @NotNull @Positive BigDecimal price,
            @NotBlank @Pattern(regexp = CURRENCY_PATTERN) String currency,
            @NotBlank @Size(max = 1000) @Pattern(regexp = URL_PATTERN) String deeplinkUrl) implements OfferRequest {
    }

    public record RentalOfferView(Long id, boolean active, long clicks30d, Long destinationId, String destination,
                                  String title, RentalType type, int capacity, int bedrooms, int bathrooms,
                                  boolean pool, boolean wifi, boolean seaView, int minNights, String description,
                                  String imageUrl, BigDecimal price, String currency, String deeplinkUrl) {
    }

    public record ActivityOfferRequest(
            @NotNull Long destinationId,
            @NotBlank @Size(max = 180) String title,
            @NotNull ActivityCategory category,
            @NotNull @DecimalMin("0.5") BigDecimal durationHours,
            @Size(max = 200) String meetingPoint,
            @Size(max = 4000) String description,
            @Size(max = 500) String imageUrl,
            @NotNull @Positive BigDecimal price,
            @NotBlank @Pattern(regexp = CURRENCY_PATTERN) String currency,
            @NotBlank @Size(max = 1000) @Pattern(regexp = URL_PATTERN) String deeplinkUrl) implements OfferRequest {
    }

    public record ActivityOfferView(Long id, boolean active, long clicks30d, Long destinationId, String destination,
                                    String title, ActivityCategory category, BigDecimal durationHours,
                                    String meetingPoint, String description, String imageUrl, BigDecimal price,
                                    String currency, String deeplinkUrl) {
    }

    public record HotelRequest(
            @NotBlank @Size(max = 180) String name,
            @NotNull Long destinationId,
            @NotNull HotelType type,
            @Min(1) @Max(5) int stars,
            @Size(max = 300) String address,
            @Size(max = 200) String locationLabel,
            @Size(max = 4000) String description,
            @Size(max = 500) String imageUrl,
            Double latitude,
            Double longitude,
            Set<@Size(max = 60) String> amenities) {
    }

    public record HotelView(Long id, String name, Long destinationId, String destination, HotelType type, int stars,
                            String address, String locationLabel, String description, String imageUrl,
                            BigDecimal rating, int reviewCount, Double latitude, Double longitude,
                            Set<String> amenities) {

        public static HotelView of(Hotel h) {
            return new HotelView(h.getId(), h.getName(), h.getDestination().getId(), h.getDestination().getName(),
                    h.getType(), h.getStars(), h.getAddress(), h.getLocationLabel(), h.getDescription(),
                    h.getImageUrl(), h.getRating(), h.getReviewCount(), h.getLatitude(), h.getLongitude(),
                    Set.copyOf(h.getAmenities()));
        }
    }
}
