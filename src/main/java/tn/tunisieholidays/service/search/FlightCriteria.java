package tn.tunisieholidays.service.search;

import tn.tunisieholidays.domain.Cabin;

import java.time.LocalDate;
import java.util.Set;

public record FlightCriteria(String origin, String destination, Set<String> originAirports,
                             Set<String> destinationAirports, LocalDate departureDate, LocalDate returnDate,
                             int adults, Cabin cabin) {

    public boolean roundTrip() {
        return returnDate != null;
    }
}
