package tn.tunisieholidays.service.search;

import tn.tunisieholidays.dto.SearchDtos.FlightLeg;

final class FlightKeys {

    private FlightKeys() {
    }

    static String key(String airlineCode, FlightLeg outbound, FlightLeg inbound) {
        StringBuilder sb = new StringBuilder(airlineCode).append('|').append(legKey(outbound));
        if (inbound != null) {
            sb.append('|').append(legKey(inbound));
        }
        return sb.toString();
    }

    private static String legKey(FlightLeg leg) {
        return leg.originCode() + leg.destinationCode() + leg.departureAt() + leg.arrivalAt();
    }
}
