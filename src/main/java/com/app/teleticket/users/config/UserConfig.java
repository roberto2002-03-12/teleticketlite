package com.app.teleticket.users.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@ApplicationScoped
public class UserConfig {

    @Inject
    @ConfigProperty(name = "cognito.region", defaultValue = "us-east-1")
    String region;

    @Produces
    @ApplicationScoped
    public CognitoIdentityProviderClient cognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }
}
