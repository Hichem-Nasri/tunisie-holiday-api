package tn.tunisieholidays.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisieholidays.domain.ActivityCategory;
import tn.tunisieholidays.domain.Board;
import tn.tunisieholidays.domain.Cabin;
import tn.tunisieholidays.domain.CarCategory;
import tn.tunisieholidays.domain.HotelType;
import tn.tunisieholidays.domain.RentalType;
import tn.tunisieholidays.domain.Transmission;
import tn.tunisieholidays.dto.SearchDtos.ActivityResult;
import tn.tunisieholidays.dto.SearchDtos.CarResult;
import tn.tunisieholidays.dto.SearchDtos.FlightResult;
import tn.tunisieholidays.dto.SearchDtos.HotelResult;
import tn.tunisieholidays.dto.SearchDtos.PickupLocation;
import tn.tunisieholidays.dto.SearchDtos.RentalResult;
import tn.tunisieholidays.dto.SearchDtos.SearchResponse;
import tn.tunisieholidays.service.search.ActivitySearchService;
import tn.tunisieholidays.service.search.CarSearchService;
import tn.tunisieholidays.service.search.FlightSearchService;
import tn.tunisieholidays.service.search.HotelSearchService;
import tn.tunisieholidays.service.search.RentalSearchService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final FlightSearchService flightSearchService;
    private final HotelSearchService hotelSearchService;
    private final CarSearchService carSearchService;
    private final RentalSearchService rentalSearchService;
    private final ActivitySearchService activitySearchService;

    @GetMapping("/flights")
    public SearchResponse<FlightResult> flights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "ECONOMY") Cabin cabin,
            @RequestParam(defaultValue = "TND") String currency,
            @RequestParam(required = false) Integer maxStops,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "PRICE") FlightSearchService.Sort sort) {
        return flightSearchService.search(origin, destination, departureDate, returnDate, adults, cabin, currency,
                maxStops, maxPrice, sort);
    }

    @GetMapping("/hotels")
    public SearchResponse<HotelResult> hotels(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "2") int adults,
            @RequestParam(defaultValue = "0") int children,
            @RequestParam(defaultValue = "1") int rooms,
            @RequestParam(defaultValue = "TND") String currency,
            @RequestParam(required = false) Set<Integer> stars,
            @RequestParam(required = false) Set<Board> boards,
            @RequestParam(required = false) Set<HotelType> types,
            @RequestParam(required = false) Set<String> amenities,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean freeCancellation,
            @RequestParam(defaultValue = "RECOMMENDED") HotelSearchService.Sort sort) {
        return hotelSearchService.search(new HotelSearchService.Criteria(q, checkIn, checkOut, adults, children,
                rooms, currency, stars, boards, types, amenities, maxPrice, freeCancellation, hotelId, sort));
    }

    @GetMapping("/cars/locations")
    public List<PickupLocation> carLocations() {
        return carSearchService.locations();
    }

    @GetMapping("/cars")
    public SearchResponse<CarResult> cars(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoffAt,
            @RequestParam(defaultValue = "30") int driverAge,
            @RequestParam(defaultValue = "TND") String currency,
            @RequestParam(required = false) Set<CarCategory> categories,
            @RequestParam(required = false) Transmission transmission,
            @RequestParam(defaultValue = "false") boolean freeCancellation,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return carSearchService.search(new CarSearchService.Criteria(location, pickupAt, dropoffAt, driverAge,
                currency, categories, transmission, freeCancellation, maxPrice));
    }

    @GetMapping("/rentals")
    public SearchResponse<RentalResult> rentals(
            @RequestParam(required = false) String q,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "2") int guests,
            @RequestParam(defaultValue = "TND") String currency,
            @RequestParam(required = false) Set<RentalType> types,
            @RequestParam(defaultValue = "false") boolean pool,
            @RequestParam(defaultValue = "false") boolean seaView,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return rentalSearchService.search(new RentalSearchService.Criteria(q, checkIn, checkOut, guests, currency,
                types, pool, seaView, maxPrice));
    }

    @GetMapping("/activities")
    public SearchResponse<ActivityResult> activities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") int participants,
            @RequestParam(defaultValue = "TND") String currency,
            @RequestParam(required = false) Set<ActivityCategory> categories,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return activitySearchService.search(new ActivitySearchService.Criteria(q, date, participants, currency,
                categories, maxPrice));
    }
}
