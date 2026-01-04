package tn.tunisieconnect.reservation.service;

import tn.tunisieconnect.reservation.dto.HostRevenueResponse;
import tn.tunisieconnect.reservation.dto.MonthlyRevenueResponse;
import tn.tunisieconnect.reservation.dto.ReservationResponse;
import tn.tunisieconnect.reservation.dto.TopListingRevenueResponse;
import tn.tunisieconnect.reservation.entity.Reservation;
import tn.tunisieconnect.reservation.entity.ReservationStatus;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

public interface HostReservationService {

    public List<ReservationResponse> getMyReservations(User owner);

    ReservationResponse updateStatus(Long reservationId, ReservationStatus newStatus, User owner);

    HostRevenueResponse getRevenues(User owner);

    List<MonthlyRevenueResponse> getMonthlyRevenues(User owner);

    List<TopListingRevenueResponse> getTopListings(User owner);

    ReservationResponse markAsPaid(Long reservationId, User owner);
}
