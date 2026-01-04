package tn.tunisieconnect.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.auth.entity.PasswordResetToken;
import tn.tunisieconnect.auth.repository.PasswordResetTokenRepository;
import tn.tunisieconnect.auth.service.PasswordResetService;
import tn.tunisieconnect.mail.EmailService;
import tn.tunisieconnect.user.entity.User;
import tn.tunisieconnect.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User not found"));

        // supprimer ancien token
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(user).expiryDate(LocalDateTime.now().plusMinutes(15)).build();

        tokenRepository.save(resetToken);

        try {
            emailService.sendResetPasswordEmail(user.getEmail(), token);
        } catch (Exception ex) {
            // Option MVP: on log et on ne bloque pas
            // (ou tu peux throw si tu veux un comportement strict)
            System.out.println("Email send failed: " + ex.getMessage());
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }
}
