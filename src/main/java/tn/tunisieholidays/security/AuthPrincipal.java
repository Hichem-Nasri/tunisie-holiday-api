package tn.tunisieholidays.security;

import tn.tunisieholidays.domain.Role;

public record AuthPrincipal(Long userId, String email, Role role, Long partnerId) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
