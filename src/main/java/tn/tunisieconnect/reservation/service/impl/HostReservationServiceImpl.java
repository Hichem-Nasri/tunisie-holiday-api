package tn.tunisieconnect.reservation.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.reservation.dto.HostRevenueResponse;
import tn.tunisieconnect.reservation.dto.MonthlyRevenueResponse;
import tn.tunisieconnect.reservation.dto.ReservationResponse;
import tn.tunisieconnect.reservation.dto.TopListingRevenueResponse;
import tn.tunisieconnect.reservation.entity.PaymentStatus;
import tn.tunisieconnect.reservation.entity.Reservation;
import tn.tunisieconnect.reservation.entity.ReservationStatus;
import tn.tunisieconnect.reservation.repository.ReservationRepository;
import tn.tunisieconnect.reservation.service.HostReservationService;
import tn.tunisieconnect.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HostReservationServiceImpl implements HostReservationService {

    private final ReservationRepository reservationRepository;

    public List<ReservationResponse> getMyReservations(User owner) {

        return reservationRepository.findByListingOwnerIdOrderByCheckInDateDesc(owner.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    public ReservationResponse updateStatus(Long reservationId, ReservationStatus newStatus, User owner) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 🔐 Sécurité : l’hôte doit être propriétaire de l’annonce
        if (!reservation.getListing().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden");
        }

        ReservationStatus current = reservation.getStatus();

        // 🧠 Règles métier MVP
        if (current == ReservationStatus.PENDING && (newStatus == ReservationStatus.CONFIRMED || newStatus == ReservationStatus.CANCELLED)) {

            reservation.setStatus(newStatus);

        } else if (current == ReservationStatus.CONFIRMED && newStatus == ReservationStatus.CANCELLED) {

            reservation.setStatus(newStatus);

        } else {
            throw new RuntimeException("Invalid status transition");
        }

        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    @Override
    public HostRevenueResponse getRevenues(User owner) {
        LocalDate now = LocalDate.now();

        return HostRevenueResponse.builder().totalRevenue(reservationRepository.totalRevenue(owner.getId())).monthlyRevenue(reservationRepository.monthlyRevenue(owner.getId(), now.getMonthValue(), now.getYear())).confirmedReservations(reservationRepository.confirmedCount(owner.getId())).build();
    }

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenues(User owner) {

        int year = LocalDate.now().getYear();

        List<Object[]> rows = reservationRepository.monthlyRevenue(owner.getId(), year);

        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put((Integer) r[0], (BigDecimal) r[1]);
        }

        List<MonthlyRevenueResponse> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(MonthlyRevenueResponse.builder().month(m).revenue(map.getOrDefault(m, BigDecimal.ZERO)).build());
        }
        return result;
    }

    @Override
    public List<TopListingRevenueResponse> getTopListings(User owner) {

        List<Object[]> rows = reservationRepository.topListings(owner.getId());

        return rows.stream().limit(5).map(r -> TopListingRevenueResponse.builder().listingId((Long) r[0]).title((String) r[1]).city((String) r[2]).revenue((BigDecimal) r[3]).reservationsCount((Long) r[4]).build()).toList();
    }

    @Override
    @Transactional
    public ReservationResponse markAsPaid(Long reservationId, User owner) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 🔒 Sécurité : l’hôte doit être propriétaire de l’annonce
        if (!r.getListing().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied");
        }

        // ✅ Règle métier MVP
        if (r.getPaymentStatus() == PaymentStatus.PAID) {
            return mapToResponse(r); // déjà payé
        }

        r.setPaymentStatus(PaymentStatus.PAID);
        r.setPaidAt(LocalDateTime.now());

        return mapToResponse(reservationRepository.save(r));
    }



    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder().id(reservation.getId()).listingId(reservation.getListing().getId()).listingTitle(reservation.getListing().getTitle()).city(reservation.getListing().getCity()).checkInDate(reservation.getCheckInDate()).checkOutDate(reservation.getCheckOutDate()).guests(reservation.getGuests()).totalPrice(reservation.getTotalPrice()).status(reservation.getStatus()).createdAt(reservation.getCreatedAt()).build();
    }

}
