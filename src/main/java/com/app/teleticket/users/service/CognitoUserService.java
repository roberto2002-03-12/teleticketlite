package com.app.teleticket.users.service;

public interface CognitoUserService {

    void adminCreateUser(String email, String phoneNumber, String role, String password);

    void adminDeleteUser(String email);
}
