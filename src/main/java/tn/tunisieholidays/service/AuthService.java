package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.AppUser;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.domain.Role;
import tn.tunisieholidays.dto.AuthDtos.AuthResponse;
import tn.tunisieholidays.dto.AuthDtos.LoginRequest;
import tn.tunisieholidays.dto.AuthDtos.RegisterPartnerRequest;
import tn.tunisieholidays.dto.AuthDtos.UserView;
import tn.tunisieholidays.dto.PartnerDtos.PartnerView;
import tn.tunisieholidays.repository.AppUserRepository;
import tn.tunisieholidays.repository.PartnerRepository;
import tn.tunisieholidays.security.AuthPrincipal;
import tn.tunisieholidays.security.JwtService;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(AppUser::isEnabled)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Email ou mot de passe incorrect"));
        if (user.getPartner() != null && user.getPartner().getStatus() == PartnerStatus.SUSPENDED) {
            throw ApiException.forbidden("Ce compte partenaire est suspendu");
        }
        return response(user);
    }

    @Transactional
    public AuthResponse registerPartner(RegisterPartnerRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Un compte existe déjà avec cet email");
        }
        Partner partner = new Partner();
        partner.setName(request.partnerName().trim());
        partner.setType(request.type());
        partner.setStatus(PartnerStatus.PENDING);
        partner.setEmail(email);
        partner.setPhone(request.phone());
        partner.setWebsite(request.website());
        partner.setCity(request.city());
        partnerRepository.save(partner);

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(request.contactName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.PARTNER);
        user.setPartner(partner);
        userRepository.save(user);
        return response(user);
    }

    @Transactional(readOnly = true)
    public UserView me(AuthPrincipal principal) {
        return userRepository.findById(principal.userId()).map(this::view)
                .orElseThrow(() -> ApiException.unauthorized("Session expirée"));
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(jwtService.generate(user), jwtService.expiresAt(), view(user));
    }

    private UserView view(AppUser user) {
        return new UserView(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getPartner() == null ? null : PartnerView.of(user.getPartner()));
    }
}
