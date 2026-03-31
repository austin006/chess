package server.websocket;

import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final SQLAuthDAO authDAO = new SQLAuthDAO();
    private final SQLGameDAO gameDAO = new SQLGameDAO();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(action, ctx.session);
                case LEAVE -> leave(action, ctx.session);
                case RESIGN -> resign(action, ctx.session);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand action, Session session) throws Exception {
        String authToken = action.getAuthToken();
        int gameID = action.getGameID();

        var authData = authDAO.getAuth(authToken);
        var gameData = gameDAO.getGame(gameID);
        String playerColor = gameData.whiteUsername().equals(authData.username()) ? "WHITE" : "BLACK";

        // This should send an error message back
        if (authData == null || gameData == null) return;

        connections.add(gameID, authToken, session);
        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(new Gson().toJson(loadGameMsg));

        var message = String.format("%s joined the game as %s", authData.username(), playerColor);
        var notificationMsg = new NotificationMessage(message);
        connections.broadcast(gameID, authToken, notificationMsg);
    }

    private void leave(UserGameCommand action, Session session) throws Exception {
        String authToken = action.getAuthToken();
        int gameID = action.getGameID();

        var authData = authDAO.getAuth(authToken);
        if (authData == null) return;

        connections.remove(gameID, authToken);

        var message = String.format("%s left the game", authData.username());
        var notificationMsg = new NotificationMessage(message);
        connections.broadcast(gameID, authToken, notificationMsg);
    }

    private void resign(UserGameCommand action, Session session) throws Exception {
        String authToken = action.getAuthToken();
        int gameID = action.getGameID();

        var gameData = gameDAO.getGame(gameID);
        var authData = authDAO.getAuth(authToken);

        if (authData == null) return;

        // Can't resign if you aren't a player
        boolean isWhite = authData.username().equals(gameData.whiteUsername());
        boolean isBlack = authData.username().equals(gameData.blackUsername());
        if (!isWhite && !isBlack) {
            var error = new ErrorMessage("Error: Observers cannot resign.");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Can't resign twice
        if (gameData.game().isGameOver()) {
            var error = new ErrorMessage("Error: The game is already over.");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        gameData.game().setGameOver(true);
        gameDAO.updateGame(gameData);

        var message = String.format("%s resigned\nThe game is finished", authData.username());
        var notificationMsg = new NotificationMessage(message);
        connections.broadcast(gameID, "", notificationMsg);
    }
}