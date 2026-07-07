package com.app.teleticket.auth.service.impl;

import com.app.teleticket.auth.dto.LoginRequest;
import com.app.teleticket.auth.dto.LoginResponse;
import com.app.teleticket.auth.exception.AuthException;
import com.app.teleticket.auth.service.AuthLoginService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@ApplicationScoped
public class AuthLoginServiceImpl implements AuthLoginService {

    @Inject
    CognitoIdentityProviderClient cognito;

    @Inject
    @ConfigProperty(name = "cognito.client-id")
    String clientId;

    @Inject
    @ConfigProperty(name = "cognito.client-secret")
    String clientSecret;

    @Override
    public LoginResponse login(LoginRequest request) {
        String secretHash = computeSecretHash(request.email, clientId, clientSecret);
        try {
            InitiateAuthResponse response = cognito.initiateAuth(InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", request.email,
                            "PASSWORD", request.password,
                            "SECRET_HASH", secretHash
                    ))
                    .build());

            var result = response.authenticationResult();
            return new LoginResponse(
                    result.accessToken(),
                    result.idToken(),
                    result.refreshToken(),
                    result.expiresIn(),
                    result.tokenType()
            );
        } catch (CognitoIdentityProviderException e) {
            throw new AuthException(401, "Authentication failed: " + e.getMessage());
        }
    }

    static String computeSecretHash(String username, String clientId, String clientSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal((username + clientId).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new AuthException(500, "Could not compute SECRET_HASH: " + e.getMessage());
        }
    }
}
