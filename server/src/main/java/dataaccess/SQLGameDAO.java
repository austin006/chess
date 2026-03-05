package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import chess.ChessGame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {
    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to clear games: %s", e.getMessage()));
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        var serializer = new Gson();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setInt(1, game.gameID());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, game.gameName());
            ps.setString(5, serializer.toJson(game.game()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create game: %s", e.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        var serializer = new Gson();

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setInt(1, gameID);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = serializer.fromJson(rs.getString("game"), ChessGame.class);

                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        var serializer = new Gson();

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                ChessGame chessGame = serializer.fromJson(rs.getString("game"), ChessGame.class);

                result.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to list games: %s", e.getMessage()));
        }

        return result;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        var serializer = new Gson();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, serializer.toJson(game.game()));
            ps.setInt(5, game.gameID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update game: %s", e.getMessage()));
        }
    }
}