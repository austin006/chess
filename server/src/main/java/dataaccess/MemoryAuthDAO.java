package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private final HashMap<String, AuthData> authDAOHashMap = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        authDAOHashMap.clear();
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        authDAOHashMap.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDAOHashMap.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDAOHashMap.remove(authToken);
    }
}
