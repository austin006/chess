package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTests {

    private SQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    // Clear test
    @Test
    @DisplayName("Clear AuthData")
    public void clearSuccess() throws DataAccessException {
        AuthData authData = new AuthData("authToken", "testUser");
        authDAO.createAuth(authData);

        authDAO.clear();

        assertNull(authDAO.getAuth("authToken"));
    }

    // Create Auth Tests
    @Test
    @DisplayName("Create Auth - Positive")
    public void createAuthSuccess() throws DataAccessException {
        AuthData expectedData = new AuthData("authToken", "testUser");
        authDAO.createAuth(expectedData);

        AuthData actualData = authDAO.getAuth("authToken");

        assertNotNull(actualData);
        assertEquals(expectedData.authToken(), actualData.authToken());
        assertEquals(expectedData.username(), actualData.username());
    }

    @Test
    @DisplayName("Create Auth - Negative")
    public void createAuthFail() throws DataAccessException {
        AuthData authData = new AuthData("authToken", "testUser");
        authDAO.createAuth(authData);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(authData);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("error"));
    }

    // Get Auth Tests
    @Test
    @DisplayName("Get Auth - Positive")
    public void getAuthSuccess() throws DataAccessException {
        AuthData expectedData = new AuthData("authToken", "testUser");
        authDAO.createAuth(expectedData);

        AuthData actualData = authDAO.getAuth("authToken");

        assertNotNull(actualData);
        assertEquals(expectedData.authToken(), actualData.authToken());
        assertEquals(expectedData.username(), actualData.username());
    }

    @Test
    @DisplayName("Get Auth - Negative")
    public void getAuthFail() throws DataAccessException {
        AuthData ghostData = authDAO.getAuth("ghostAuthToken");
        assertNull(ghostData);
    }

    // Delete Auth Tests
    @Test
    @DisplayName("Delete Auth - Positive")
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData authData = new AuthData("authToken", "testUser");
        authDAO.createAuth(authData);
        authDAO.deleteAuth("authToken");

        assertNull(authDAO.getAuth("authToken"));
    }

    @Test
    @DisplayName("Delete Auth - Negative")
    public void deleteAuthFail() throws DataAccessException {
        authDAO.deleteAuth("authToken");
        assertNull(authDAO.getAuth("authToken"));
    }
}
