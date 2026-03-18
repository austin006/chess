package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // Check the Request
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if username is already taken
        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Hash the password
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        // Create the User
        UserData newUser = new UserData(request.username(), hashedPassword, request.email());
        userDAO.createUser(newUser);

        // Generate an Auth Token -> this logs the user in
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, request.username());
        authDAO.createAuth(newAuth);

        // Return the successful Result
        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        // Check the Request
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if username exists
        if (userDAO.getUser(request.username()) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Check if password is incorrect
        if (!BCrypt.checkpw(request.password(), userDAO.getUser(request.username()).password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Generate an Auth Token -> this logs the user in
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, request.username());
        authDAO.createAuth(newAuth);

        // Return the successful Result
        return new LoginResult(request.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        // Check if token exists
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Delete the Auth Token -> this logs the user out
        authDAO.deleteAuth(authToken);
    }
}
