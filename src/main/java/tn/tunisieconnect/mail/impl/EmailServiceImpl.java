package tn.tunisieconnect.mail.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.mail.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(String to, String name) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bienvenue sur Tunisie Holiday 🇹🇳");
        message.setText("""
                Bonjour %s,

                Votre compte hôte a été créé avec succès 🎉

                Vous pouvez maintenant vous connecter et commencer
                à publier vos logements.

                À très bientôt,
                L’équipe Tunisie Holiday
                """.formatted(name));

        mailSender.send(message);
    }

    @Override
    public void sendResetPasswordEmail(String to, String token) {

        String link = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de mot de passe");
        message.setText("""
            Vous avez demandé une réinitialisation de mot de passe.

            Cliquez sur le lien suivant :
            %s

            Ce lien expire dans 15 minutes.
            """.formatted(link));

        mailSender.send(message);
    }

}

