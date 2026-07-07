package com.app.teleticket.auth.service;

import com.app.teleticket.auth.dto.LoginRequest;
import com.app.teleticket.auth.dto.LoginResponse;

public interface AuthLoginService {

    LoginResponse login(LoginRequest request);
}
