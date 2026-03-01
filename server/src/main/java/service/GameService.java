package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

import java.util.Objects;
import java.util.UUID;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        // Check if user is correctly logged in
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return new ListGamesResult(gameDAO.listGames());
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest createGameRequest) throws DataAccessException {
        // Check if user is correctly logged in
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Check request isn't null
        if (createGameRequest.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Add game to database
        int gameID = Math.abs(UUID.randomUUID().hashCode());
        GameData newGame = new GameData(gameID, null, null, createGameRequest.gameName(), new ChessGame());
        gameDAO.createGame(newGame);

        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest joinGameRequest) throws DataAccessException {
        // Check if user is correctly logged in
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Check the game exists
        GameData currentGame = gameDAO.getGame(joinGameRequest.gameID());
        if (currentGame == null) {
            throw new DataAccessException("Error: bad request");
        }

        // check team color is black or white
        if (!Objects.equals(joinGameRequest.playerColor(), "WHITE") && !Objects.equals(joinGameRequest.playerColor(), "BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        // Update username
        String blackUser = currentGame.blackUsername();
        String whiteUser = currentGame.whiteUsername();
        if (Objects.equals(joinGameRequest.playerColor(), "WHITE")) {
            if (whiteUser != null) {
                throw new DataAccessException("Error: already taken");
            }
            whiteUser = authData.username();
        } else if (Objects.equals(joinGameRequest.playerColor(), "BLACK")) {
            if (blackUser != null) {
                throw new DataAccessException("Error: already taken");
            }
            blackUser = authData.username();
        }

        // Update game
        GameData updatedGame = new GameData(currentGame.gameID(), whiteUser, blackUser, currentGame.gameName(), currentGame.game());
        gameDAO.updateGame(updatedGame);
    }
}
