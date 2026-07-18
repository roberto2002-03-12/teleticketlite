package com.app.teleticket.auth.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@ApplicationScoped
public class UserConfig {

    @Inject
    @ConfigProperty(name = "cognito.region", defaultValue = "us-east-1")
    String region;

    @Inject
    @ConfigProperty(name = "aws.credentials.access-key-id")
    String accessKeyId;

    @Inject
    @ConfigProperty(name = "aws.credentials.secret-access-key")
    String secretAccessKey;

    // USAR CUANDO PASES A LABROLE
    // @Inject
    // @ConfigProperty(name = "aws.credentials.session-token")
    // String sessionToken;

    @Produces
    @ApplicationScoped
    public CognitoIdentityProviderClient cognitoClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                // .credentialsProvider(StaticCredentialsProvider.create(
                //         AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)))
                .build();
    }
}