package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.request.LoginRequest;
import service.request.LogoutRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        getUser(username);
        createUser(userData);
        createAuth(authData);
    }
    public LoginResult login(LoginRequest loginRequest) {
        getUser(username);
        createAuth(username);
    }
    public void logout(LogoutRequest logoutRequest) {
        getAuth(authToken);
        deleteAuth(authToken);
    }
}
