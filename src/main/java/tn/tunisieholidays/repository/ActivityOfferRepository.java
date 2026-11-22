package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.ActivityOffer;
import tn.tunisieholidays.domain.PartnerStatus;

import java.util.List;

public interface ActivityOfferRepository extends JpaRepository<ActivityOffer, Long> {

    @Query("""
            select a from ActivityOffer a join fetch a.partner p join fetch a.destination d
            where a.active = true and p.status = :status and d.id in :destinationIds
            """)
    List<ActivityOffer> findActiveByDestinations(@Param("destinationIds") List<Long> destinationIds,
                                                 @Param("status") PartnerStatus status);

    @Query("""
            select a from ActivityOffer a join fetch a.partner p join fetch a.destination d
            where a.active = true and p.status = :status
            order by a.price asc
            """)
    List<ActivityOffer> findAllActive(@Param("status") PartnerStatus status);

    @Query("select a from ActivityOffer a join fetch a.destination where a.partner.id = :partnerId order by a.createdAt desc")
    List<ActivityOffer> findByPartnerId(@Param("partnerId") Long partnerId);

    long countByPartnerId(Long partnerId);
}
