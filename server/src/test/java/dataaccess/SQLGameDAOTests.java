package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTests {

    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    // Clear test
    @Test
    @DisplayName("Clear GameData")
    public void clearSuccess() throws DataAccessException {
        GameData gameData = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(gameData);

        gameDAO.clear();

        assertNull(gameDAO.getGame(1234));
    }

    // Create game tests
    @Test
    @DisplayName("Create Game - Positive")
    public void createGameSuccess() throws DataAccessException {
        GameData expectedGame = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(expectedGame);

        GameData actualGame = gameDAO.getGame(1234);

        assertNotNull(actualGame);
        assertEquals(expectedGame.gameID(), actualGame.gameID());
        assertEquals(expectedGame.whiteUsername(), actualGame.whiteUsername());
        assertEquals(expectedGame.blackUsername(), actualGame.blackUsername());
        assertEquals(expectedGame.gameName(), actualGame.gameName());

        assertEquals(expectedGame.game(), actualGame.game());
    }

    @Test
    @DisplayName("Create Game - Negative")
    public void createGameFail() throws DataAccessException {
        GameData gameData = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(gameData);

        // duplicate create
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(gameData);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("error"));
    }

    // Get game tests
    @Test
    @DisplayName("Get Game - Positive")
    public void getGameSuccess() throws DataAccessException {
        createGameSuccess();
    }

    @Test
    @DisplayName("Get Game - Negative")
    public void getGameFail() throws DataAccessException {
        GameData ghostGame = gameDAO.getGame(1234);
        assertNull(ghostGame);
    }

    // List game tests
    @Test
    @DisplayName("List Game - Positive")
    public void listGameSuccess() throws DataAccessException {
        // create two games
        GameData game1 = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(game1);
        GameData game2 = new GameData(6789, "white2", "black2", "gameName2", new ChessGame());
        gameDAO.createGame(game2);

        // list games
        Collection<GameData> result = gameDAO.listGames();

        // Check it is not null
        assertNotNull(result);

        // check there are two games
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("List Game - Negative")
    public void listGameFail() throws DataAccessException {
        // list games on an empty database
        Collection<GameData> result = gameDAO.listGames();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Update game tests
    @Test
    @DisplayName("Update Game - Positive")
    public void updateGameSuccess() throws DataAccessException {
        GameData game1 = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(game1);
        GameData expectedGame = new GameData(1234, "white2", "black2", "gameName2", new ChessGame());
        gameDAO.updateGame(expectedGame);

        GameData actualGame = gameDAO.getGame(1234);

        assertNotNull(actualGame);
        assertEquals(expectedGame.gameID(), actualGame.gameID());
        assertEquals(expectedGame.whiteUsername(), actualGame.whiteUsername());
        assertEquals(expectedGame.blackUsername(), actualGame.blackUsername());
        assertEquals(expectedGame.gameName(), actualGame.gameName());
        assertEquals(expectedGame.game(), actualGame.game());
    }

    @Test
    @DisplayName("Update Game - Negative")
    public void updateGameFail() throws DataAccessException {
        GameData game1 = new GameData(1234, "white", "black", "gameName", new ChessGame());
        gameDAO.createGame(game1);

        GameData badUpdate = new GameData(1234, "white", "black", null, new ChessGame());

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(badUpdate);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("error"));
    }
}