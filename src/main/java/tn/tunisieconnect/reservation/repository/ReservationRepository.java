package tn.tunisieconnect.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.tunisieconnect.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByListingOwnerIdOrderByCheckInDateDesc(Long ownerId);

    @Query("""
              select coalesce(sum(r.totalPrice), 0)
              from Reservation r
              where r.listing.owner.id = :ownerId
                and r.status = 'CONFIRMED'
            """)
    BigDecimal totalRevenue(Long ownerId);

    @Query("""
              select coalesce(sum(r.totalPrice), 0)
              from Reservation r
              where r.listing.owner.id = :ownerId
                and r.status = 'CONFIRMED'
                and month(r.checkInDate) = :month
                and year(r.checkInDate) = :year
            """)
    BigDecimal monthlyRevenue(Long ownerId, int month, int year);

    @Query("""
              select count(r)
              from Reservation r
              where r.listing.owner.id = :ownerId
                and r.status = 'CONFIRMED'
            """)
    Long confirmedCount(Long ownerId);

    @Query("""
              select month(r.checkInDate), coalesce(sum(r.totalPrice), 0)
              from Reservation r
              where r.listing.owner.id = :ownerId
                and r.status = 'CONFIRMED'
                and year(r.checkInDate) = :year
              group by month(r.checkInDate)
              order by month(r.checkInDate)
            """)
    List<Object[]> monthlyRevenue(Long ownerId, int year);

    @Query("""
              select
                r.listing.id,
                r.listing.title,
                r.listing.city,
                sum(r.totalPrice),
                count(r)
              from Reservation r
              where r.listing.owner.id = :ownerId
                and r.status = 'CONFIRMED'
              group by r.listing.id, r.listing.title, r.listing.city
              order by sum(r.totalPrice) desc
            """)
    List<Object[]> topListings(Long ownerId);

}
