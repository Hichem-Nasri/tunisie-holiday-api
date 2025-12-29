package tn.tunisieconnect.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom complet de l'hôte
    @Column(nullable = false)
    private String name;

    // Email utilisé pour login
    @Column(nullable = false, unique = true)
    private String email;

    // Mot de passe hashé (BCrypt)
    @Column(nullable = false)
    private String password;

    // Type de compte : OWNER / HOTEL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    // Actif ou non (future gestion)
    @Column(nullable = false)
    private boolean enabled = true;
}
