package tn.tunisieholidays.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tn.tunisieholidays.domain.Airport;
import tn.tunisieholidays.domain.Destination;
import tn.tunisieholidays.domain.ExchangeRate;
import tn.tunisieholidays.domain.OfferType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record DestinationView(Long id, String name, String slug, String region, String tagline,
                                  String description, String imageUrl, String airportCode, boolean popular,
                                  int sortOrder) {

        public static DestinationView of(Destination d) {
            return new DestinationView(d.getId(), d.getName(), d.getSlug(), d.getRegion(), d.getTagline(),
                    d.getDescription(), d.getImageUrl(), d.getAirportCode(), d.isPopular(), d.getSortOrder());
        }
    }

    public record DestinationRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Pattern(regexp = "^[a-z0-9-]{2,120}$") String slug,
            @Size(max = 120) String region,
            @Size(max = 200) String tagline,
            @Size(max = 4000) String description,
            @Size(max = 500) String imageUrl,
            @Pattern(regexp = "^[A-Z]{3}$") String airportCode,
            boolean popular,
            int sortOrder) {
    }

    public record AirportView(String code, String name, String city, String cityCode, String countryCode) {

        public static AirportView of(Airport a) {
            return new AirportView(a.getCode(), a.getName(), a.getCity(), a.getCityCode(), a.getCountryCode());
        }
    }

    public record CurrencyView(String code, String label, String symbol, BigDecimal rateToTnd, Instant updatedAt) {

        public static CurrencyView of(ExchangeRate r) {
            return new CurrencyView(r.getCurrency(), r.getLabel(), r.getSymbol(), r.getRateToTnd(), r.getUpdatedAt());
        }
    }

    public record RateUpdateRequest(@NotNull @Positive BigDecimal rateToTnd) {
    }

    public record DealView(OfferType type, String title, String subtitle, String badge, BigDecimal price,
                           String currency, String priceUnit, String imageUrl, String searchPath) {
    }

    public record PriceAlertRequest(
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String origin,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String destination,
            @NotNull @Future LocalDate departureDate,
            LocalDate returnDate,
            @Positive BigDecimal maxPrice,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {
    }
}
