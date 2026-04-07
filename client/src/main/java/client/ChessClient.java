package client;

import java.util.Arrays;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.LoginResult;
import result.RegisterResult;
import ui.BoardPrinter;
import ui.EscapeSequences;
import websocket.ServerMessageObserver;
import websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade server;
    private State state = State.SIGNED_OUT;
    private GameData[] gameList;

    private WebSocketFacade ws;
    private final String serverUrl;
    private String playerColor;
    private chess.ChessGame currentGame;
    private int currentGameID;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public void run() {
        System.out.println("♛ Welcome to CS240 Chess ♛");
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
        System.out.print("\n\n" + "[" + state + "] >>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                // SIGNED_OUT commands
                case "register" -> register(params);
                case "login" -> login(params);

                // SIGNED_IN commands
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);

                // IN_GAME commands
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "highlight" -> highlight(params);

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
            return String.format("You signed in as %s", result.username());
        }
        throw new ResponseException(400, "Expected: register <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            LoginRequest request = new LoginRequest(params[0], params[1]);
            LoginResult result = server.login(request);
            state = State.SIGNED_IN;
            return String.format("You signed in as %s", result.username());
        }
        throw new ResponseException(400, "Expected: login <username> <password>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout();
        state = State.SIGNED_OUT;
        return "You signed out";
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var result = server.listGames();
        this.gameList = result.games().toArray(new GameData[0]);

        var sb = new StringBuilder();
        sb.append("Current Games:\n");
        for (int i = 0; i < gameList.length; i++) {
            GameData game = gameList[i];
            sb.append(String.format("%d. %s | White: %s | Black: %s",
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
                    return "Invalid game number\nPlease run 'list' to see available games";
                }
                currentGameID = gameList[uiIndex - 1].gameID();
                playerColor = params[0].toUpperCase();

                JoinGameRequest request = new JoinGameRequest(playerColor, currentGameID);
                server.joinGame(request);

                // Connect to websocket
                if (ws == null) {
                    ws = new WebSocketFacade(serverUrl, this);
                }
                ws.connect(server.getAuthToken(), currentGameID);
                state = State.IN_GAME;

                return String.format("Joining game %d as %s...", uiIndex, playerColor);

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID";
            }
        }
        throw new ResponseException(400, "Expected: join [WHITE|BLACK] <ID>");
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();

        if (params.length == 1) {
            try {
                int uiIndex = Integer.parseInt(params[0]);
                if (gameList == null || uiIndex < 1 || uiIndex > gameList.length) {
                    return "Invalid game number\nPlease run 'list' to see available games";
                }
                int realGameID = gameList[uiIndex - 1].gameID();
                playerColor = "WHITE";

                JoinGameRequest request = new JoinGameRequest(playerColor, realGameID);
                server.joinGame(request);

                state = State.IN_GAME;

                // Print out board
                ChessGame game = gameList[uiIndex - 1].game();
                if(game != null) {
                    BoardPrinter.printBoard(game.getBoard(), playerColor);
                }

                return "You are observing the game";

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID";
            }
        }
        throw new ResponseException(400, "Expected: observe <ID>");
    }

    public String redraw() throws ResponseException {
        assertInGame();
        BoardPrinter.printBoard(currentGame.getBoard(), playerColor);
        return "";
    }

    public String leave() throws ResponseException {
        assertInGame();
        ws.leave(server.getAuthToken(), currentGameID);
        currentGame = null;
        state = State.SIGNED_IN;

        return "You left the game";
    }

    public String move(String... params) throws ResponseException {
        assertInGame();
        if (params.length != 2 && params.length != 3) {
            throw new ResponseException(400, "Expected: move <start> <end> [promotionPiece]\nExample: move d2 c3");
        }

        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);
        ChessPiece.PieceType promotionPiece = null;

        if (params.length == 3) {
            promotionPiece = switch (params[2].toLowerCase()) {
                case "queen" -> ChessPiece.PieceType.QUEEN;
                case "rook" -> chess.ChessPiece.PieceType.ROOK;
                case "bishop" -> chess.ChessPiece.PieceType.BISHOP;
                case "knight" -> chess.ChessPiece.PieceType.KNIGHT;
                default -> throw new ResponseException(400, "Invalid promotion piece. Use: queen, rook, bishop, or knight.");
            };
        }

        ChessMove move = new ChessMove(start, end, promotionPiece);
        ws.makeMove(server.getAuthToken(), currentGameID, move);
        return "Move sent to server...";
    }

    public String resign() throws ResponseException {
        assertInGame();
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("yes")) {
             ws.resign(server.getAuthToken(), currentGameID);
            return "You resigned the game";
        }

        return "Resignation cancelled";
    }

    public String highlight(String... params) throws ResponseException {
        assertInGame();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: highlight <position>\nExample: highlight a4");
        }
        ChessPosition position = parsePosition(params[0]);
        var validMoves = currentGame.validMoves(position);
        BoardPrinter.printBoard(currentGame.getBoard(), playerColor, position, validMoves);
        return "";
    }

    public String help() {
        String cmd = EscapeSequences.SET_TEXT_COLOR_BLUE;
        String desc = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String reset = EscapeSequences.RESET_TEXT_COLOR;

        if (state == State.SIGNED_OUT) {
            return cmd + "  register <USERNAME> <PASSWORD> <EMAIL>" + desc + " - Create an account\n" +
                    cmd + "  login <USERNAME> <PASSWORD>" + desc + " - Sign in to play chess\n" +
                    cmd + "  quit" + desc + " - Stop the application\n" +
                    cmd + "  help" + desc + " - List available commands" + reset;
        }

        if (state == State.IN_GAME) {
            return cmd + "  redraw" + desc + " - Redraw the chess board\n" +
                    cmd + "  leave" + desc + " - Leave the game and return to menu\n" +
                    cmd + "  move <START> <END>" + desc + " - Make a move\n" +
                    cmd + "  resign" + desc + " - Resign and forfeit the game\n" +
                    cmd + "  highlight <PIECE>" + desc + " - Highlight all legal moves for selected piece\n" +
                    cmd + "  help" + desc + " - List available commands" + reset;
        }

        return cmd + "  create <NAME>" + desc + " - Create a new game\n" +
                cmd + "  list" + desc + " - List existing games\n" +
                cmd + "  join [WHITE|BLACK] <ID>" + desc + " - Join a game\n" +
                cmd + "  observe <ID>" + desc + " - Watch a game\n" +
                cmd + "  logout" + desc + " - Sign out of chess\n" +
                cmd + "  quit" + desc + " - Stop the application\n" +
                cmd + "  help" + desc + " - List available commands" + reset;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
        if (state == State.IN_GAME) {
            throw new ResponseException(400, "You are currently in a game");
        }
    }

    private void assertInGame() throws ResponseException {
        if (state != State.IN_GAME) {
            throw new ResponseException(400, "You must be in a game");
        }
    }

    private ChessPosition parsePosition(String pos) throws ResponseException {
        if (pos == null || pos.length() != 2) {
            throw new ResponseException(400, "Invalid position format. Expected: e2, a5, etc.");
        }
        int col = pos.toLowerCase().charAt(0) - 'a' +1;
        int row = pos.charAt(1) -'0';
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new ResponseException(400, "Position out of bounds. Must be between a1 and h8.");
        }
        return new ChessPosition(row, col);
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> {
                var notification = (NotificationMessage) message;
                System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN +
                        notification.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                printPrompt();
            }
            case ERROR -> {
                 var error = (ErrorMessage) message;
                 System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED +
                                   error.getErrorMessage() + EscapeSequences.RESET_TEXT_COLOR);
                 printPrompt();
            }
            case LOAD_GAME -> {
                var loadGame = (LoadGameMessage) message;
                this.currentGame = loadGame.getGame();
                BoardPrinter.printBoard(currentGame.getBoard(), playerColor);
                printPrompt();
            }
        }
    }
}