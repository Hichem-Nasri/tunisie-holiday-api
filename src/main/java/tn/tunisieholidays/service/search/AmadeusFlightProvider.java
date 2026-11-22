package tn.tunisieholidays.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tn.tunisieholidays.config.AppProperties;
import tn.tunisieholidays.dto.SearchDtos.FlightLeg;
import tn.tunisieholidays.dto.SearchDtos.FlightOption;
import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AmadeusFlightProvider implements FlightProvider {

    private final AppProperties.Amadeus config;
    private final RestClient client;
    private String accessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public AmadeusFlightProvider(AppProperties properties, RestClient.Builder builder) {
        this.config = properties.amadeus();
        this.client = builder.baseUrl(config.baseUrl() == null ? "https://test.api.amadeus.com" : config.baseUrl()).build();
    }

    @Override
    public String name() {
        return "AMADEUS";
    }

    @Override
    public boolean enabled() {
        return config.enabled() && config.clientId() != null && !config.clientId().isBlank()
                && config.clientSecret() != null && !config.clientSecret().isBlank();
    }

    @Override
    public List<FlightResult> search(FlightCriteria c, Converter converter) {
        if (!enabled()) {
            return List.of();
        }
        try {
            JsonNode response = client.get()
                    .uri(uri -> {
                        uri.path("/v2/shopping/flight-offers")
                                .queryParam("originLocationCode", c.origin())
                                .queryParam("destinationLocationCode", c.destination())
                                .queryParam("departureDate", c.departureDate())
                                .queryParam("adults", c.adults())
                                .queryParam("travelClass", c.cabin().name())
                                .queryParam("max", config.maxResults() > 0 ? config.maxResults() : 30);
                        if (c.returnDate() != null) {
                            uri.queryParam("returnDate", c.returnDate());
                        }
                        return uri.build();
                    })
                    .header("Authorization", "Bearer " + token())
                    .retrieve()
                    .body(JsonNode.class);
            return map(response, c, converter);
        } catch (RestClientException ex) {
            log.warn("Recherche Amadeus indisponible : {}", ex.getMessage());
            return List.of();
        }
    }

    private synchronized String token() {
        if (accessToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return accessToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", config.clientId());
        form.add("client_secret", config.clientSecret());
        JsonNode body = client.post()
                .uri("/v1/security/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (body == null || !body.hasNonNull("access_token")) {
            throw new RestClientException("Jeton Amadeus absent de la réponse");
        }
        accessToken = body.get("access_token").asText();
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, body.path("expires_in").asLong(1799) - 60));
        return accessToken;
    }

    List<FlightResult> map(JsonNode response, FlightCriteria c, Converter converter) {
        List<FlightResult> results = new ArrayList<>();
        if (response == null || !response.has("data")) {
            return results;
        }
        JsonNode carriers = response.path("dictionaries").path("carriers");
        for (JsonNode offer : response.get("data")) {
            JsonNode itineraries = offer.path("itineraries");
            if (itineraries.isEmpty()) {
                continue;
            }
            FlightLeg out = leg(itineraries.get(0));
            FlightLeg in = itineraries.size() > 1 ? leg(itineraries.get(1)) : null;
            String airline = offer.path("validatingAirlineCodes").path(0)
                    .asText(itineraries.get(0).path("segments").path(0).path("carrierCode").asText());
            String airlineName = carriers.path(airline).asText(airline);
            BigDecimal total = new BigDecimal(offer.path("price").path("grandTotal").asText("0"));
            BigDecimal perPerson = total.divide(BigDecimal.valueOf(c.adults()), 2, RoundingMode.HALF_UP);
            BigDecimal price = converter.convert(perPerson, offer.path("price").path("currency").asText("EUR"));
            JsonNode bags = offer.path("travelerPricings").path(0).path("fareDetailsBySegment").path(0)
                    .path("includedCheckedBags");
            boolean baggage = bags.path("quantity").asInt(0) > 0 || bags.path("weight").asInt(0) > 0;
            String redirect = "/api/go/amadeus?origin=" + c.origin() + "&destination=" + c.destination()
                    + "&departureDate=" + c.departureDate() + "&adults=" + c.adults()
                    + (c.returnDate() != null ? "&returnDate=" + c.returnDate() : "");
            FlightOption option = new FlightOption("A-" + offer.path("id").asText(), "AMADEUS", airlineName, price,
                    converter.target(), baggage, redirect);
            long duration = out.durationMinutes() + (in == null ? 0 : in.durationMinutes());
            int stops = Math.max(out.stops(), in == null ? 0 : in.stops());
            results.add(new FlightResult(FlightKeys.key(airline, out, in), airline, airlineName, out, in,
                    List.of(option), price, converter.target(), duration, stops));
        }
        return results;
    }

    private FlightLeg leg(JsonNode itinerary) {
        JsonNode segments = itinerary.path("segments");
        JsonNode first = segments.get(0);
        JsonNode last = segments.get(segments.size() - 1);
        LocalDateTime departure = LocalDateTime.parse(first.path("departure").path("at").asText());
        LocalDateTime arrival = LocalDateTime.parse(last.path("arrival").path("at").asText());
        long minutes = itinerary.hasNonNull("duration")
                ? Duration.parse(itinerary.get("duration").asText()).toMinutes()
                : Duration.between(departure, arrival).toMinutes();
        String flightNumber = first.path("carrierCode").asText() + " " + first.path("number").asText();
        return new FlightLeg(first.path("departure").path("iataCode").asText(),
                last.path("arrival").path("iataCode").asText(), departure, arrival, minutes,
                segments.size() - 1, flightNumber);
    }
}
