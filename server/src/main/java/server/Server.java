package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import server.websocket.WebSocketHandler;
import service.GameService;
import service.UserService;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserService userService;
    private final GameService gameService;
    private final WebSocketHandler webSocketHandler = new WebSocketHandler();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            SQLUserDAO.configureDatabase();
        } catch (DataAccessException e) {
            System.out.println("Couldn't create database: " + e.getMessage());
        }

        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });

        javalin.exception(DataAccessException.class, (exception, context) -> {
            String errorMessage = new Gson().toJson(Map.of("message", exception.getMessage()));
            context.result(errorMessage);

            if (exception.getMessage().contains("bad request")) {
                context.status(400);
            } else if (exception.getMessage().contains("unauthorized")) {
                context.status(401);
            } else if (exception.getMessage().contains("already taken")) {
                context.status(403);
            } else {
                context.status(500);
            }
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
    
    private void clearHandler(Context context) throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();

        context.status(200);
        context.result("{}");
    }

    private void registerHandler(Context context) throws DataAccessException {
        var serializer = new Gson();

        // Convert JSON to Java object
        RegisterRequest registerRequest = serializer.fromJson(context.body(), RegisterRequest.class);

        // Call service
        RegisterResult result = userService.register(registerRequest);

        // Set the status code
        context.status(200);
        context.result(serializer.toJson(result));
    }

    private void loginHandler(Context context) throws DataAccessException {
        var serializer = new Gson();

        // Convert JSON to Java object
        LoginRequest loginRequest = serializer.fromJson(context.body(), LoginRequest.class);

        // Call service
        LoginResult result = userService.login(loginRequest);

        // Set the status code
        context.status(200);
        context.result(serializer.toJson(result));
    }

    private void logoutHandler(Context context) throws DataAccessException {
        // Get the user's authToken
        String authToken = context.header("authorization");

        // Call service
        userService.logout(authToken);

        // Set the status code
        context.status(200);
        context.result("{}");
    }

    private void listGamesHandler(Context context) throws DataAccessException {
        var serializer = new Gson();

        // Get the user's authToken
        String authToken = context.header("authorization");

        // Call service
        ListGamesResult result = gameService.listGames(authToken);

        // Set the status code
        context.status(200);
        context.result(serializer.toJson(result));
    }

    private void createGameHandler(Context context) throws DataAccessException {
        var serializer = new Gson();

        // Get the user's authToken
        String authToken = context.header("authorization");

        // Convert JSON to Java object
        CreateGameRequest createGameRequest = serializer.fromJson(context.body(), CreateGameRequest.class);

        // Call service
        CreateGameResult result = gameService.createGame(authToken, createGameRequest);

        // Set the status code
        context.status(200);
        context.result(serializer.toJson(result));
    }

    private void joinGameHandler(Context context) throws DataAccessException {
        var serializer = new Gson();

        // Get the user's authToken
        String authToken = context.header("authorization");

        // Convert JSON to Java object
        JoinGameRequest joinGameRequest = serializer.fromJson(context.body(), JoinGameRequest.class);

        // Call service
        gameService.joinGame(authToken, joinGameRequest);

        // Set the status code
        context.status(200);
        context.result("{}");
    }
}