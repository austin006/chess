package service;

import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);

        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
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
    @Test
    public void listGamesSuccess() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        // save authToken
        String authToken = registerResult.authToken();

        // create 2 games
        CreateGameRequest request1 = new CreateGameRequest("cool_game");
        gameService.createGame(authToken, request1);
        CreateGameRequest request2 = new CreateGameRequest("epic_game");
        gameService.createGame(authToken, request2);

        // list games
        ListGamesResult result = gameService.listGames(authToken);

        // Check it is not null
        assertNotNull(result);

        // check there are two games
        assertEquals(2, result.games().size());
    }

    // Negative test - bad token
    @Test
    public void listGamesFail() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        // save authToken and bad one too
        String authToken = registerResult.authToken();
        String authTokenBad = "bad_authToken";

        // create 2 games
        CreateGameRequest request1 = new CreateGameRequest("cool_game");
        gameService.createGame(authToken, request1);
        CreateGameRequest request2 = new CreateGameRequest("epic_game");
        gameService.createGame(authToken, request2);

        // try to list games
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(authTokenBad);
        });

        // Check error message matches
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // JOINGAME TESTS
    // Positive test
    @Test
    public void joinGamesSuccess() throws DataAccessException {
        // register person in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        // save authToken
        String authToken = registerResult.authToken();

        // create game
        CreateGameRequest createGameRequest = new CreateGameRequest("cool_game");
        CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);

        // Join game
        JoinGameRequest request = new JoinGameRequest("WHITE", createGameResult.gameID());
        gameService.joinGame(authToken, request);

        // Check it's updated in database
        assertEquals("chessplayer", gameDAO.getGame(createGameResult.gameID()).whiteUsername());
    }

    // Negative test - Spot already taken
    @Test
    public void joinGamesFail() throws DataAccessException {
        // register 2 people in database
        RegisterRequest registerRequest = new RegisterRequest("chessplayer", "epic_password", "chess@gmail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        RegisterRequest registerRequest2 = new RegisterRequest("chessplayer2", "epic_password2", "chess2@gmail.com");
        RegisterResult registerResult2 = userService.register(registerRequest2);

        // save authToken
        String authToken = registerResult.authToken();
        String authToken2 = registerResult2.authToken();

        // create game
        CreateGameRequest createGameRequest = new CreateGameRequest("cool_game");
        CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);

        // Join game
        JoinGameRequest request = new JoinGameRequest("WHITE", createGameResult.gameID());
        gameService.joinGame(authToken, request);

        // try to join game with second person
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(authToken2, request);
        });

        // Check error message matches
        assertEquals("Error: already taken", exception.getMessage());
    }
}