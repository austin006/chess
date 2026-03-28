package server.websocket;

import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
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

        // This should send an error message back
        if (authData == null || gameData == null) return;

        connections.add(gameID, authToken, session);
        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(new Gson().toJson(loadGameMsg));

        var message = String.format("%s joined the game", authData.username());
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
}