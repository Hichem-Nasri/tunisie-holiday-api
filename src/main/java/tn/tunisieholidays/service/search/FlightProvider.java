package tn.tunisieholidays.service.search;

import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.service.CurrencyService.Converter;

import java.util.List;

public interface FlightProvider {

    String name();

    boolean enabled();

    List<FlightResult> search(FlightCriteria criteria, Converter converter);
}
