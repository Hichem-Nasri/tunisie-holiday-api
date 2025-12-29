package tn.tunisieconnect.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.auth.dto.AuthResponse;
import tn.tunisieconnect.auth.dto.LoginRequest;
import tn.tunisieconnect.auth.dto.RegisterRequest;
import tn.tunisieconnect.auth.service.AuthService;
import tn.tunisieconnect.user.entity.User;
import tn.tunisieconnect.user.entity.UserType;
import tn.tunisieconnect.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final JwtService jwtService; // à venir

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already used");
        }

        User user = User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).type(UserType.valueOf(request.type().toUpperCase())).enabled(true).build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 🔐 JWT plus tard
        String token = "JWT_TOKEN_MOCK";

        return new AuthResponse(token);
    }
}
