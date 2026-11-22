package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.Cabin;
import tn.tunisieholidays.domain.FlightOffer;
import tn.tunisieholidays.domain.PartnerStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FlightOfferRepository extends JpaRepository<FlightOffer, Long> {

    @Query("""
            select f from FlightOffer f join fetch f.partner p
            where f.active = true and p.status = :status
              and f.originCode in :origins and f.destinationCode in :destinations
              and f.departureAt >= :from and f.departureAt < :to
              and f.seatsAvailable >= :seats and f.cabin = :cabin
            """)
    List<FlightOffer> search(@Param("origins") Collection<String> origins,
                             @Param("destinations") Collection<String> destinations,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to,
                             @Param("seats") int seats,
                             @Param("cabin") Cabin cabin,
                             @Param("status") PartnerStatus status);

    @Query("select f from FlightOffer f where f.partner.id = :partnerId and f.departureAt >= :from order by f.departureAt asc")
    List<FlightOffer> findByPartnerIdFrom(@Param("partnerId") Long partnerId, @Param("from") LocalDateTime from);

    long countByPartnerId(Long partnerId);
}
