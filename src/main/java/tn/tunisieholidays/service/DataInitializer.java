package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.config.AppProperties;
import tn.tunisieholidays.domain.AppUser;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.Role;
import tn.tunisieholidays.repository.AppUserRepository;
import tn.tunisieholidays.repository.PartnerRepository;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Map<String, String> DEMO_ACCOUNTS = Map.of(
            "agence@demo.tn", "Carthage Voyages",
            "loueur@demo.tn", "Carthage Rent",
            "hote@demo.tn", "Villas du Cap",
            "loisirs@demo.tn", "Sahara Expériences");

    private final AppUserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Bootstrap bootstrap = properties.bootstrap();
        if (bootstrap.adminEmail() != null && !bootstrap.adminEmail().isBlank()
                && !userRepository.existsByEmailIgnoreCase(bootstrap.adminEmail())) {
            AppUser admin = new AppUser();
            admin.setEmail(bootstrap.adminEmail().toLowerCase());
            admin.setFullName("Administrateur");
            admin.setPasswordHash(passwordEncoder.encode(bootstrap.adminPassword()));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Compte administrateur créé : {}", admin.getEmail());
        }
        if (bootstrap.demoPartnerPassword() == null || bootstrap.demoPartnerPassword().isBlank()) {
            return;
        }
        DEMO_ACCOUNTS.forEach((email, partnerName) -> {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                return;
            }
            partnerRepository.findAll().stream()
                    .filter(p -> p.getName().equals(partnerName))
                    .findFirst()
                    .ifPresent(partner -> createDemoUser(email, partner, bootstrap.demoPartnerPassword()));
        });
    }

    private void createDemoUser(String email, Partner partner, String password) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName("Démo " + partner.getName());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.PARTNER);
        user.setPartner(partner);
        userRepository.save(user);
        log.info("Compte partenaire de démonstration créé : {}", email);
    }
}
