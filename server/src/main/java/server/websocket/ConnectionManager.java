package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String authToken, Session session) {
        var gameConnections = connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());
        gameConnections.put(authToken, session);
    }

    public void remove(int gameID, String authToken) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            gameConnections.remove(authToken);
            if (gameConnections.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage notification) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            String msg = new Gson().toJson(notification);

            for (var entry : gameConnections.entrySet()) {
                String authToken = entry.getKey();
                Session session = entry.getValue();

                if (session.isOpen()) {
                    if (!authToken.equals(excludeAuthToken)) {
                        session.getRemote().sendString(msg);
                    }
                }
            }
        }
    }
}