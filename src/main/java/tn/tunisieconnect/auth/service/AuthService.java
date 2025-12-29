package tn.tunisieconnect.auth.service;

import tn.tunisieconnect.auth.dto.AuthResponse;
import tn.tunisieconnect.auth.dto.LoginRequest;
import tn.tunisieconnect.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
