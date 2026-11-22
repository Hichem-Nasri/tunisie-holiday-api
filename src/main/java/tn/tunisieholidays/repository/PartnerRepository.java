package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.PartnerStatus;

import java.util.List;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findByStatusOrderByCreatedAtDesc(PartnerStatus status);

    List<Partner> findAllByOrderByCreatedAtDesc();

    long countByStatus(PartnerStatus status);
}
