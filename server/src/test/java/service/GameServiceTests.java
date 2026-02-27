package service;

import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.request.CreateGameRequest;
import service.request.RegisterRequest;
import service.result.CreateGameResult;
import service.result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
    }

    // CREATEGAME TESTS
    // Positive test
    @Test
    public void createGameSuccess() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        // save authToken
        String authToken = registerResult.authToken();

        // create game
        CreateGameRequest request = new CreateGameRequest("cool_game");
        CreateGameResult result = gameService.createGame(authToken, request);

        assertNotNull(result); // check it's not null
        assertNotNull(gameDAO.getGame(result.gameID())); // check it saved in the DAO
        assertEquals("cool_game", gameDAO.getGame(result.gameID()).gameName()); // check name matches
    }

    // Negative test - bad authToken
    @Test
    public void createGameFail() throws DataAccessException {
        // bad authToken
        String authToken = "bad_authToken";

        // create game
        CreateGameRequest request = new CreateGameRequest("cool_game");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(authToken, request);
        });

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // LISTGAMES TESTS
    // Positive test

    // Negative test - bad token

    // JOINGAME TESTS
    // Positive test

    // Negative test - Spot already taken

}