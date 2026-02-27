package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    // REGISTER TESTS
    // Positive test
    @Test
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult result = userService.register(request);

        assertNotNull(result); // check it's not null
        assertEquals("chessplayer", result.username()); // check username matches
        assertNotNull(result.authToken()); // check it has an authToken

        // check it saved in the DAO
        assertNotNull(userDAO.getUser("chessplayer"));
    }

    // Negative test
    @Test
    public void registerDuplicateUserFail() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        userService.register(request);

        // Try to register same person again
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(request);
        });

        // Check the error message matches
        assertEquals("Error: already taken", exception.getMessage());
    }

    // LOGIN TESTS
    // Positive test
    @Test
    public void loginSuccess() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        userService.register(registerRequest);

        // login
        LoginRequest request = new LoginRequest("chessplayer", "epic_password");
        LoginResult result = userService.login(request);

        assertNotNull(result); // check it's not null
        assertEquals("chessplayer", result.username()); // check username matches
        assertNotNull(result.authToken()); // check it has an authToken
    }

    // Negative test
    @Test
    public void loginGhostUserFail() throws DataAccessException {
        // try to login without registering
        LoginRequest request = new LoginRequest("chessplayer", "epic_password");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login(request);
        });

        // Check the error message matches
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // Negative test
    @Test
    public void loginWrongPassword() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        userService.register(registerRequest);

        // try to login with wrong password
        LoginRequest request = new LoginRequest("chessplayer", "wrong_password");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login(request);
        });

        // Check the error message matches
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // LOGOUT TESTS
    // Positive test
    @Test
    public void logoutSuccess() throws DataAccessException {
        // register person in database
        RegisterRequest request = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult result = userService.register(request);

        // save authToken
        String authToken = result.authToken();

        // logout
        userService.logout(authToken);

        // check they're logged out by not having an authToken anymore
        assertNull(authDAO.getAuth(authToken));
    }

    // Negative test
    @Test
    public void logoutFail() throws DataAccessException {
        // try to logout with fake token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.logout("fake_token");
        });

        assertEquals("Error: unauthorized", exception.getMessage());
    }
}