package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.ClickEvent;

import java.time.Instant;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByPartnerIdAndCreatedAtAfter(Long partnerId, Instant from);

    long countByCreatedAtAfter(Instant from);

    @Query(value = """
            select cast(date_trunc('day', created_at at time zone 'Africa/Tunis') as date) as day, count(*) as clicks
            from click_events
            where (cast(:partnerId as bigint) is null or partner_id = cast(:partnerId as bigint)) and created_at >= :from
            group by 1 order by 1
            """, nativeQuery = true)
    List<Object[]> countPerDay(@Param("partnerId") Long partnerId, @Param("from") Instant from);

    @Query(value = """
            select offer_type, count(*) from click_events
            where (cast(:partnerId as bigint) is null or partner_id = cast(:partnerId as bigint)) and created_at >= :from
            group by offer_type order by 2 desc
            """, nativeQuery = true)
    List<Object[]> countPerType(@Param("partnerId") Long partnerId, @Param("from") Instant from);

    @Query(value = """
            select p.id, p.name, count(c.id) from click_events c
            join partners p on p.id = c.partner_id
            where c.created_at >= :from
            group by p.id, p.name order by 3 desc
            """, nativeQuery = true)
    List<Object[]> countPerPartner(@Param("from") Instant from);

    @Query(value = """
            select offer_id, count(*) from click_events
            where partner_id = :partnerId and offer_type = :offerType and created_at >= :from and offer_id is not null
            group by offer_id
            """, nativeQuery = true)
    List<Object[]> countPerOffer(@Param("partnerId") Long partnerId, @Param("offerType") String offerType,
                                 @Param("from") Instant from);
}
