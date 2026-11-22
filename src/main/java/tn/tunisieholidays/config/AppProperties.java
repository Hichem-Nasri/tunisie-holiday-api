package tn.tunisieholidays.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Bootstrap bootstrap, Amadeus amadeus) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Bootstrap(String adminEmail, String adminPassword, String demoPartnerPassword) {
    }

    public record Amadeus(boolean enabled, String baseUrl, String clientId, String clientSecret,
                          int maxResults, String bookingUrlTemplate) {
    }
}
