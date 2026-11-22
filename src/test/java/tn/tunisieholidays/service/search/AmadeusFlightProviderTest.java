package tn.tunisieholidays.service.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tn.tunisieholidays.config.AppProperties;
import tn.tunisieholidays.domain.Cabin;
import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.service.CurrencyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AmadeusFlightProviderTest {

    private static final String RESPONSE = """
            {
              "data": [{
                "id": "1",
                "itineraries": [
                  {"duration": "PT2H45M", "segments": [{"departure": {"iataCode": "ORY", "at": "2026-06-12T07:40:00"},
                    "arrival": {"iataCode": "DJE", "at": "2026-06-12T10:25:00"}, "carrierCode": "TU", "number": "723"}]},
                  {"duration": "PT2H50M", "segments": [{"departure": {"iataCode": "DJE", "at": "2026-06-21T11:30:00"},
                    "arrival": {"iataCode": "ORY", "at": "2026-06-21T14:20:00"}, "carrierCode": "TU", "number": "724"}]}
                ],
                "price": {"currency": "EUR", "grandTotal": "590.00"},
                "validatingAirlineCodes": ["TU"],
                "travelerPricings": [{"fareDetailsBySegment": [{"includedCheckedBags": {"quantity": 1}}]}]
              }],
              "dictionaries": {"carriers": {"TU": "TUNISAIR"}}
            }
            """;

    @Test
    void mapsAmadeusOffersPerPerson() throws Exception {
        AppProperties properties = new AppProperties(null, null, null,
                new AppProperties.Amadeus(true, "https://test.api.amadeus.com", "id", "secret", 10, "https://x/{origin}"));
        AmadeusFlightProvider provider = new AmadeusFlightProvider(properties, RestClient.builder());
        FlightCriteria criteria = new FlightCriteria("PAR", "DJE", Set.of("ORY", "CDG"), Set.of("DJE"),
                LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 21), 2, Cabin.ECONOMY);
        CurrencyService.Converter converter = new CurrencyService.Converter(
                Map.of("TND", BigDecimal.ONE, "EUR", new BigDecimal("3.40")), "EUR");

        List<FlightResult> results = provider.map(new ObjectMapper().readTree(RESPONSE), criteria, converter);

        assertThat(results).hasSize(1);
        FlightResult r = results.get(0);
        assertThat(r.airlineName()).isEqualTo("TUNISAIR");
        assertThat(r.minPrice()).isEqualByComparingTo("295.00");
        assertThat(r.outbound().durationMinutes()).isEqualTo(165);
        assertThat(r.inbound().flightNumber()).isEqualTo("TU 724");
        assertThat(r.offers().get(0).baggageIncluded()).isTrue();
        assertThat(r.offers().get(0).redirectUrl()).startsWith("/api/go/amadeus?origin=PAR");
    }
}
