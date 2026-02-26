package service;

public class GameService {
    public ListGamesResult listgames(ListGamesRequest listGamesRequest) {
        getAuth(authToken);
        listGames();
    }
    public CreateGameResult createGame(CreateGameRequest createGameRequest) {
        getAuth(authToken);
        createGame(gameName);
    }
    public void joinGame(JoinGameRequest joinGameRequest) {
        getAuth(authToken);
        getGame(gameID);
        updateGame(gameID);
    }
}
