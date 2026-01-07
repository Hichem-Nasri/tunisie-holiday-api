package tn.tunisieconnect.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.tunisieconnect.reservation.dto.HostRevenueResponse;
import tn.tunisieconnect.reservation.dto.MonthlyRevenueResponse;
import tn.tunisieconnect.reservation.dto.ReservationResponse;
import tn.tunisieconnect.reservation.dto.TopListingRevenueResponse;
import tn.tunisieconnect.reservation.entity.Reservation;
import tn.tunisieconnect.reservation.entity.ReservationStatus;
import tn.tunisieconnect.reservation.service.HostReservationService;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/host/reservations")
@RequiredArgsConstructor
public class HostReservationController {

    private final HostReservationService reservationService;

    @GetMapping
    public List<ReservationResponse> myReservations(Authentication authentication) {

        User owner = (User) authentication.getPrincipal();
        return reservationService.getMyReservations(owner);
    }

    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(@PathVariable Long id, @RequestParam ReservationStatus status, Authentication authentication) {
        User owner = (User) authentication.getPrincipal();
        return reservationService.updateStatus(id, status, owner);
    }

    @GetMapping("/revenues")
    public HostRevenueResponse revenues(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return reservationService.getRevenues(owner);
    }

    @GetMapping("/revenues/monthly")
    public List<MonthlyRevenueResponse> monthly(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return reservationService.getMonthlyRevenues(owner);
    }

    @GetMapping("/revenues/top-listings")
    public List<TopListingRevenueResponse> topListings(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return reservationService.getTopListings(owner);
    }

    @PatchMapping("/{id}/mark-paid")
    public ReservationResponse markAsPaid(@PathVariable Long id, Authentication authentication) {
        User owner = (User) authentication.getPrincipal();
        return  reservationService.markAsPaid(id, owner);
    }

}
