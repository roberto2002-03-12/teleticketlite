package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.service.CognitoUserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

@ApplicationScoped
public class CognitoUserServiceImpl implements CognitoUserService {

    @Inject
    CognitoIdentityProviderClient cognito;

    @Inject
    @ConfigProperty(name = "cognito.user-pool-id")
    String userPoolId;

    @Override
    public void adminCreateUser(String email, String phoneNumber, String role, String password) {
        try {
            AdminCreateUserResponse response = cognito.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("phone_number").value(phoneNumber).build()
                    )
                    .messageAction("SUPPRESS")
                    .build());

            cognito.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(response.user().username())
                    .groupName(role)
                    .build());

            cognito.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(response.user().username())
                    .password(password)
                    .permanent(true)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw new UserException(502, "Cognito registration failed: " + e.getMessage());
        }
    }

    @Override
    public void adminDeleteUser(String email) {
        try {
            cognito.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw new UserException(502, "Cognito deletion failed: " + e.getMessage());
        }
    }

    @Override
    public void addToGroup(String email, String group) {
        try {
            cognito.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .groupName(group)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw new UserException(502, "Cognito add-to-group failed: " + e.getMessage());
        }
    }
}
