package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.request.*;
import service.result.*;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws ResponseException{
        facade.clear();
    }

    // Register tests
    @Test
    @DisplayName("Register Positive")
    void registerSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = facade.register(request);

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Register Negative")
    void registerFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = facade.register(request);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.register(request);
        });
        assertEquals(403, exception.statusCode());
    }

    // Login tests
    @Test
    @DisplayName("Login Positive")
    void loginSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult registerResult = facade.register(request);
        LoginRequest loginRequest = new LoginRequest("player1", "password");
        LoginResult result = facade.login(loginRequest);

        assertNotNull(result);
        assertEquals(request.username(), result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Login Negative")
    void loginFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        LoginRequest loginRequest = new LoginRequest("player1", "WRONG_password");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.login(loginRequest);
        });
        assertEquals(401, exception.statusCode());
    }

    // Logout tests
    @Test
    @DisplayName("Logout Positive")
    void logoutSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        CreateGameResult createGameResult = facade.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest("white", createGameResult.gameID());
        facade.logout();

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.joinGame(joinGameRequest);
        });
        assertEquals(401, exception.statusCode());
    }

    @Test
    @DisplayName("Logout Negative")
    void logoutFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        facade.logout();

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.logout();
        });
        assertEquals(401, exception.statusCode());
    }

    // List Games tests
    @Test
    @DisplayName("List Games Positive")
    void listGamesSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        facade.createGame(createGameRequest);
        CreateGameRequest createGameRequest2 = new CreateGameRequest("epicGame2");
        facade.createGame(createGameRequest2);

        ListGamesResult result = facade.listGames();
        assertNotNull(result);
        assertEquals(2, result.games().size());
    }

    @Test
    @DisplayName("List Games Negative")
    void listGamesFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        facade.createGame(createGameRequest);
        CreateGameRequest createGameRequest2 = new CreateGameRequest("epicGame2");
        facade.createGame(createGameRequest2);

        facade.logout();
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.listGames();
        });
        assertEquals(401, exception.statusCode());
    }

    // Create Game tests
    @Test
    @DisplayName("Create Game Positive")
    void createGameSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        CreateGameResult result = facade.createGame(createGameRequest);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    @DisplayName("Create Game Negative")
    void createGameFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        facade.logout();
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.createGame(createGameRequest);
        });
        assertEquals(401, exception.statusCode());
    }

    // Join Game tests
    @Test
    @DisplayName("Join Game Positive")
    void joinGameSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        CreateGameResult createGameResult = facade.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameID());
        facade.joinGame(joinGameRequest);

        ListGamesResult listResult = facade.listGames();
        var game = listResult.games().iterator().next();
        assertEquals("player1", game.whiteUsername());
    }

    @Test
    @DisplayName("Join Game Negative")
    void joinGameFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        facade.register(request);
        CreateGameRequest createGameRequest = new CreateGameRequest("epicGame");
        facade.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest("white", 89745645);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.joinGame(joinGameRequest);
        });

        assertEquals(400, exception.statusCode());
    }
}
