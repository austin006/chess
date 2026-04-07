package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
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
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(moveCommand, ctx.session);
                }
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

        if (authData == null || gameData == null) {
            var error = new ErrorMessage("Error: Unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        String username = authData.username();
        String role = "an observer";
        if (username.equals(gameData.whiteUsername())) role = "White";
        else if (username.equals(gameData.blackUsername())) role = "Black";

        connections.add(gameID, authToken, session);
        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(new Gson().toJson(loadGameMsg));

        var message = String.format("%s joined the game as %s", authData.username(), role);
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
            var error = new ErrorMessage("Error: Observers cannot resign");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Can't resign twice
        if (gameData.game().isGameOver()) {
            var error = new ErrorMessage("Error: The game is already over");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        gameData.game().setGameOver(true);
        gameDAO.updateGame(gameData);

        var message = String.format("%s resigned\nThe game is finished", authData.username());
        var notificationMsg = new NotificationMessage(message);
        connections.broadcast(gameID, "", notificationMsg);
    }

    private void makeMove(MakeMoveCommand action, Session session) throws Exception {
        String authToken = action.getAuthToken();
        int gameID = action.getGameID();
        ChessMove move = action.getMove();

        var gameData = gameDAO.getGame(gameID);
        var authData = authDAO.getAuth(authToken);

        if (authData == null) {
            var error = new ErrorMessage("Error: Unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        // Can't move if the game is over
        if (gameData.game().isGameOver()) {
            var error = new ErrorMessage("Error: Game is already over");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Can't move if you are an observer (not a player)
        boolean isWhite = authData.username().equals(gameData.whiteUsername());
        boolean isBlack = authData.username().equals(gameData.blackUsername());
        if (!isWhite && !isBlack) {
            var error = new ErrorMessage("Error: Observers cannot make moves");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Check it is that player's turn
        boolean isBlackTurn = gameData.game().getTeamTurn() == ChessGame.TeamColor.BLACK;
        boolean isWhiteTurn = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE;
        if (!((isWhiteTurn && isWhite) || (isBlackTurn && isBlack))) {
            var error = new ErrorMessage("Error: It must be your turn to make a move");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Make sure move is valid
        try {
            gameData.game().makeMove(move);
        } catch (InvalidMoveException e) {
            var error = new ErrorMessage("Error: Move was invalid");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        // Update the game in the database
        gameDAO.updateGame(gameData);
        // Broadcast the new board
        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(new Gson().toJson(loadGameMsg));
        connections.broadcast(gameID, authToken, loadGameMsg);
        // Broadcast the action
        var message = String.format("%s moved %s from %s to %s", authData.username(), gameData.game().getBoard().getPiece(move.getEndPosition()), move.getStartPosition(), move.getEndPosition());
        var notificationMsg = new NotificationMessage(message);
        connections.broadcast(gameID, authToken, notificationMsg);
        // Check the game state
        ChessGame.TeamColor opponentColor = isWhiteTurn ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String opponentName = isWhiteTurn ? gameData.blackUsername() : gameData.whiteUsername();

        if (gameData.game().isInCheckmate(opponentColor)) {
            message = String.format("%s is in checkmate!\nGame over", opponentName);
             gameData.game().setGameOver(true);
             gameDAO.updateGame(gameData);

            notificationMsg = new NotificationMessage(message);
            connections.broadcast(gameID, "", notificationMsg);
        } else if (gameData.game().isInCheck(opponentColor)) {
            message = String.format("%s is in check!", opponentName);
            notificationMsg = new NotificationMessage(message);
            connections.broadcast(gameID, "", notificationMsg);
        } else if (gameData.game().isInStalemate(opponentColor)) {
            message = String.format("%s is in stalemate!\nGame over", opponentName);
            gameData.game().setGameOver(true);
            gameDAO.updateGame(gameData);

            notificationMsg = new NotificationMessage(message);
            connections.broadcast(gameID, "", notificationMsg);
        }
    }
}