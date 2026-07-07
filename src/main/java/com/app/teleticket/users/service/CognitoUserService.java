package com.app.teleticket.users.service;

public interface CognitoUserService {

    void adminCreateUser(String email, String phoneNumber, String role);

    void adminDeleteUser(String email);
}
