package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Objects;

public class BoardPrinter {
    public static void printBoard(ChessBoard board, String playerColor) {
        boolean isWhite = (Objects.equals(playerColor, "WHITE"));

        System.out.println();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                printPiece(board.getPiece(position));

            }
            System.out.println();
        }
        System.out.println();
    }

    private static void printPiece(ChessPiece piece) {
        if(piece == null) {
            System.out.print(EscapeSequences.EMPTY);
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        } else {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        }

        switch (piece.getPieceType()) {
            case KING -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
            case QUEEN -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
            case BISHOP -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
            case KNIGHT -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
            case ROOK -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
            case PAWN -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}
