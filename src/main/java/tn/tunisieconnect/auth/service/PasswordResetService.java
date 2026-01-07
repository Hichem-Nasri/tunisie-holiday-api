package tn.tunisieconnect.auth.service;

public interface PasswordResetService {

    public void forgotPassword(String email);

    public void resetPassword(String token, String newPassword);
}
