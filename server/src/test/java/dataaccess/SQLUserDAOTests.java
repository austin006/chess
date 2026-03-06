package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {

    private SQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    // Clear test
    @Test
    @DisplayName("Clear Users")
    public void clearSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email@email.com");
        userDAO.createUser(user);

        userDAO.clear();

        assertNull(userDAO.getUser("testUser"));
    }

    // Create user tests
    @Test
    @DisplayName("Create User - Positive")
    public void createUserSuccess() throws DataAccessException {
        // Create user
        UserData expectedUser = new UserData("testUser", "password", "email@email.com");
        userDAO.createUser(expectedUser);

        // Get it from the database
        UserData actualUser = userDAO.getUser("testUser");

        // Check they match
        assertNotNull(actualUser);
        assertEquals(expectedUser.username(), actualUser.username());
        assertEquals(expectedUser.password(), actualUser.password());
        assertEquals(expectedUser.email(), actualUser.email());
    }

    @Test
    @DisplayName("Create User - Negative")
    public void createUserFailDuplicate() throws DataAccessException {
        // Create user
        UserData user = new UserData("testUser", "password", "email@email.com");
        userDAO.createUser(user);

        // Try to create same user again
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
        });

        // Check there was an error
        assertTrue(exception.getMessage().toLowerCase().contains("error"));
    }

    // Get user tests
    @Test
    @DisplayName("Get User - Positive")
    public void getUserSuccess() throws DataAccessException {
        createUserSuccess();
    }

    @Test
    @DisplayName("Get User - Negative")
    public void getUserFailGhost() throws DataAccessException {
        // Try to get a user that hasn't been added to database
        UserData ghostUser = userDAO.getUser("ghostUser");

        // Check there was a nothing returned
        assertNull(ghostUser);
    }
}