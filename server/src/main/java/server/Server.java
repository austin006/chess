package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;
import service.request.RegisterRequest;
import service.result.RegisterResult;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
//        javalin.post("/session", this::loginHandler);
//        javalin.delete("/session", this::logoutHandler);
//        javalin.get("/game", this::listGamesHandler);
//        javalin.post("/game", this::createGameHandler);
//        javalin.put("/game", this::joinGameHandler);

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
}