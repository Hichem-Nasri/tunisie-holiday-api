package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.Hotel;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("""
            select h from Hotel h join fetch h.destination d
            where lower(h.name) like lower(concat('%', :q, '%'))
               or lower(d.name) like lower(concat('%', :q, '%'))
            order by h.name
            """)
    List<Hotel> search(@Param("q") String q);

    @Query("select h from Hotel h join fetch h.destination order by h.name")
    List<Hotel> findAllWithDestination();
}
