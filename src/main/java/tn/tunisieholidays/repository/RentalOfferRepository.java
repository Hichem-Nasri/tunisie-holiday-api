package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.domain.RentalOffer;

import java.util.List;

public interface RentalOfferRepository extends JpaRepository<RentalOffer, Long> {

    @Query("""
            select r from RentalOffer r join fetch r.partner p join fetch r.destination d
            where r.active = true and p.status = :status and d.id in :destinationIds
            """)
    List<RentalOffer> findActiveByDestinations(@Param("destinationIds") List<Long> destinationIds,
                                               @Param("status") PartnerStatus status);

    @Query("select r from RentalOffer r join fetch r.destination where r.partner.id = :partnerId order by r.createdAt desc")
    List<RentalOffer> findByPartnerId(@Param("partnerId") Long partnerId);

    long countByPartnerId(Long partnerId);
}
