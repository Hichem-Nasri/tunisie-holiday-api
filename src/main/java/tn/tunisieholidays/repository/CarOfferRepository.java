package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.CarOffer;
import tn.tunisieholidays.domain.PartnerStatus;

import java.util.List;

public interface CarOfferRepository extends JpaRepository<CarOffer, Long> {

    @Query("""
            select c from CarOffer c join fetch c.partner p
            where c.active = true and p.status = :status and upper(c.pickupCode) = upper(:code)
            """)
    List<CarOffer> findActiveByPickup(@Param("code") String code, @Param("status") PartnerStatus status);

    @Query("""
            select c from CarOffer c join fetch c.partner p
            where c.active = true and p.status = :status
            order by c.price asc
            """)
    List<CarOffer> findActiveOrderByPrice(@Param("status") PartnerStatus status);

    @Query("""
            select distinct c.pickupCode, c.pickupLabel from CarOffer c
            where c.active = true and c.partner.status = :status
            order by c.pickupLabel
            """)
    List<Object[]> findPickupLocations(@Param("status") PartnerStatus status);

    List<CarOffer> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    long countByPartnerId(Long partnerId);
}
