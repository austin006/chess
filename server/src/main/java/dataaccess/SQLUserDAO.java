package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO{
    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
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
