package tn.tunisieholidays.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.domain.Role;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record RegisterPartnerRequest(
            @NotBlank @Size(max = 150) String partnerName,
            @NotNull PartnerType type,
            @NotBlank @Size(max = 150) String contactName,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @Size(max = 40) String phone,
            @Size(max = 300) String website,
            @Size(max = 100) String city) {
    }

    public record UserView(Long id, String email, String fullName, Role role, PartnerDtos.PartnerView partner) {
    }

    public record AuthResponse(String token, Instant expiresAt, UserView user) {
    }
}
