package client;

import java.util.Arrays;
import java.util.Scanner;

import chess.ChessGame;
import model.GameData;
import service.request.*;
import service.result.*;
import ui.BoardPrinter;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNED_OUT;
    private GameData[] gameList;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♛ Welcome to CS240 chess. Type Help to get started. ♛");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!"quit".equals(result)) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (result == null) {
                    result = "Error: Server connection failed or returned an empty response.";
                }
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + "[" + state + "] >>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResult result = server.register(request);
            state = State.SIGNED_IN;
            return String.format("You signed in as %s.", result.username());
        }
        throw new ResponseException(400, "Expected: register <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            LoginRequest request = new LoginRequest(params[0], params[1]);
            LoginResult result = server.login(request);
            state = State.SIGNED_IN;
            return String.format("You signed in as %s.", result.username());
        }
        throw new ResponseException(400, "Expected: login <username> <password>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout();
        state = State.SIGNED_OUT;
        return "You successfully signed out";
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var result = server.listGames();
        this.gameList = result.games().toArray(new GameData[0]);

        var sb = new StringBuilder();
        sb.append("Current Games:\n");
        for (int i = 0; i < gameList.length; i++) {
            GameData game = gameList[i];
            sb.append(String.format("%d. %s | White: %s | Black: %s\n",
                    (i + 1),
                    game.gameName(),
                    game.whiteUsername() == null ? "Empty" : game.whiteUsername(),
                    game.blackUsername() == null ? "Empty" : game.blackUsername()));
        }
        return sb.toString();
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            CreateGameRequest request = new CreateGameRequest(params[0]);
            CreateGameResult result = server.createGame(request);
            return String.format("You created a new game called %s", request.gameName());
        }
        throw new ResponseException(400, "Expected: create <gameName>");
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 2) {
            try {
                int uiIndex = Integer.parseInt(params[1]);
                if (gameList == null || uiIndex < 1 || uiIndex > gameList.length) {
                    return "Invalid game number. Please run 'list' to see available games.";
                }
                int realGameID = gameList[uiIndex - 1].gameID();
                String playerColor = params[0].toUpperCase();

                JoinGameRequest request = new JoinGameRequest(playerColor, realGameID);
                server.joinGame(request);

                // Print out board
                ChessGame game = gameList[uiIndex - 1].game();
                if(game != null) {
                    BoardPrinter.printBoard(game.getBoard(), playerColor);
                }

                return String.format("You joined the game on the %s team.", request.playerColor());

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            }
        }
        throw new ResponseException(400, "Expected: join <teamColor> <gameNumber>");
    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        return """
                create <NAME> - a game
                list - games
                join <ID> - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}