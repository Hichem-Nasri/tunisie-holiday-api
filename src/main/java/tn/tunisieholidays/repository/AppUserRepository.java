package tn.tunisieholidays.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
