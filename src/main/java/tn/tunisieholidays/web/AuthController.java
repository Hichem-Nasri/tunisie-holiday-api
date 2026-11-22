package tn.tunisieholidays.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisieholidays.dto.AuthDtos.AuthResponse;
import tn.tunisieholidays.dto.AuthDtos.LoginRequest;
import tn.tunisieholidays.dto.AuthDtos.RegisterPartnerRequest;
import tn.tunisieholidays.dto.AuthDtos.UserView;
import tn.tunisieholidays.security.AuthPrincipal;
import tn.tunisieholidays.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register-partner")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerPartner(@Valid @RequestBody RegisterPartnerRequest request) {
        return authService.registerPartner(request);
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal AuthPrincipal principal) {
        return authService.me(principal);
    }
}
