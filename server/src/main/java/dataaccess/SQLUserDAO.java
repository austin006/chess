package dataaccess;

import model.UserData;

import java.sql.*;

public class SQLUserDAO implements UserDAO{
    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to clear users: %s", e.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create user: %s", e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, username);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    static String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
              username VARCHAR(255) NOT NULL,
              password VARCHAR(255) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (username)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS authTokens (
              authToken VARCHAR(255) NOT NULL,
              username VARCHAR(255) NOT NULL,
              PRIMARY KEY (authToken)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
              gameID INT NOT NULL AUTO_INCREMENT,
              whiteUsername VARCHAR(255),
              blackUsername VARCHAR(255),
              gameName VARCHAR(255) NOT NULL,
              game_json TEXT NOT NULL,
              PRIMARY KEY (gameID)
            )
            """
    };

    public static void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
