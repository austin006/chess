package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SQLAuthDAO implements AuthDAO{
    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE authTokens";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to clear authTokens: %s", e.getMessage()));
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO authTokens (authToken, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to create authData: %s", e.getMessage()));
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM authTokens WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"), rs.getString("username"));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM authTokens WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to delete authToken: %s", e.getMessage()));
        }
    }
}
