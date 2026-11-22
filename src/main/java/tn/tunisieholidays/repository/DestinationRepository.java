package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.Destination;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findBySlug(String slug);

    List<Destination> findAllByOrderBySortOrderAscNameAsc();

    boolean existsBySlug(String slug);
}
