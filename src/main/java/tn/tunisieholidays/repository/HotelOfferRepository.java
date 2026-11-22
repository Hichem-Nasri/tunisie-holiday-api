package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.HotelOffer;
import tn.tunisieholidays.domain.PartnerStatus;

import java.util.List;

public interface HotelOfferRepository extends JpaRepository<HotelOffer, Long> {

    @Query("""
            select o from HotelOffer o
            join fetch o.partner p
            join fetch o.hotel h
            join fetch h.destination d
            where o.active = true and p.status = :status and d.id in :destinationIds
            """)
    List<HotelOffer> findActiveByDestinations(@Param("destinationIds") List<Long> destinationIds,
                                              @Param("status") PartnerStatus status);

    @Query("""
            select o from HotelOffer o
            join fetch o.partner p
            join fetch o.hotel h
            join fetch h.destination d
            where o.active = true and p.status = :status and h.id = :hotelId
            """)
    List<HotelOffer> findActiveByHotel(@Param("hotelId") Long hotelId, @Param("status") PartnerStatus status);

    @Query("""
            select o from HotelOffer o
            join fetch o.partner p
            join fetch o.hotel h
            join fetch h.destination d
            where o.active = true and p.status = :status
            order by o.price asc
            """)
    List<HotelOffer> findActiveOrderByPrice(@Param("status") PartnerStatus status);

    @Query("select o from HotelOffer o join fetch o.hotel h join fetch h.destination where o.partner.id = :partnerId order by o.createdAt desc")
    List<HotelOffer> findByPartnerId(@Param("partnerId") Long partnerId);

    long countByPartnerId(Long partnerId);
}
