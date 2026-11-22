package tn.tunisieholidays.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.service.ClickService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/go")
@RequiredArgsConstructor
public class RedirectController {

    private final ClickService clickService;

    @GetMapping("/amadeus")
    public ResponseEntity<Void> amadeus(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(defaultValue = "1") int adults,
            HttpServletRequest request) {
        return redirect(clickService.redirectToAmadeus(origin, destination, departureDate, returnDate, adults, request));
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<Void> offer(@PathVariable OfferType type, @PathVariable Long id,
                                      @RequestParam(required = false) Long returnOfferId,
                                      HttpServletRequest request) {
        return redirect(clickService.redirectToOffer(type, id, returnOfferId, request));
    }

    private ResponseEntity<Void> redirect(URI uri) {
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }
}
