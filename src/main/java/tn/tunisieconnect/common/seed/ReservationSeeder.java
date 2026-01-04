package tn.tunisieconnect.common.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.tunisieconnect.listing.entity.Listing;
import tn.tunisieconnect.listing.repository.ListingRepository;
import tn.tunisieconnect.reservation.entity.Reservation;
import tn.tunisieconnect.reservation.entity.ReservationStatus;
import tn.tunisieconnect.reservation.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationSeeder implements CommandLineRunner {

    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;

    @Override
    public void run(String... args) {
        List<Listing> listings = listingRepository.findAll();
        if (listings.isEmpty()) return;

        for (Listing listing : listings) {
            for (int i = 0; i < listings.size(); i++) {
                listing = listings.get(i);

                ReservationStatus status;
                if (i % 3 == 0) {
                    status = ReservationStatus.PENDING;
                } else if (i % 3 == 1) {
                    status = ReservationStatus.CONFIRMED;
                } else {
                    status = ReservationStatus.CANCELLED;
                }

                Reservation r = Reservation.builder().listing(listing).checkInDate(LocalDate.now().plusDays(3 + i)).checkOutDate(LocalDate.now().plusDays(6 + i)).guests(2).totalPrice(listing.getPricePerNight().multiply(BigDecimal.valueOf(3))).status(status).createdAt(LocalDateTime.now()).build();

                reservationRepository.save(r);
            }

        }
    }}
