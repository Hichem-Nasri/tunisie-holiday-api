package tn.tunisieconnect.auth.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}
