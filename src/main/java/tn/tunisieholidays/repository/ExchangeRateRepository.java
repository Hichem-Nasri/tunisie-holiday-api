package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
}
