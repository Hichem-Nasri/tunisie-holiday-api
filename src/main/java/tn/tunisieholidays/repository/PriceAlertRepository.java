package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.PriceAlert;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
}
