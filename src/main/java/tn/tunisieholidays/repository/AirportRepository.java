package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tunisieholidays.domain.Airport;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport, String> {

    List<Airport> findByCityCode(String cityCode);

    @Query("""
            select a from Airport a
            where upper(a.code) = upper(:q)
               or upper(a.cityCode) = upper(:q)
               or lower(a.city) like lower(concat(:q, '%'))
               or lower(a.name) like lower(concat('%', :q, '%'))
            order by a.countryCode desc, a.city asc
            """)
    List<Airport> search(@Param("q") String q);
}
