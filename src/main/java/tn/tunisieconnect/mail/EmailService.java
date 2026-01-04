package tn.tunisieconnect.mail;

public interface EmailService {
    void sendWelcomeEmail(String to, String name);

    void sendResetPasswordEmail(String to, String token);
}