package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardPrinter {
    public static void printBoard(ChessBoard board, String playerColor) {
        boolean isWhite = !"BLACK".equalsIgnoreCase(playerColor);

        int startRow = isWhite ? 8 : 1;
        int endRow = isWhite ? 1 : 8;
        int rowDirection = isWhite ? -1 : 1;
        int startCol = isWhite ? 1 : 8;
        int endCol = isWhite ? 8 : 1;
        int colDirection = isWhite ? 1 : -1;

        System.out.println();
        printHeaders(isWhite);
        for (int row = startRow; row != endRow + rowDirection; row += rowDirection) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_BOLD);
            System.out.print(" " + row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);

            for (int col = startCol; col != endCol + colDirection; col += colDirection) {
                boolean isLightSquare = (row + col) % 2 != 0;

                if (isLightSquare) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                }

                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                printPiece(piece);
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_BOLD);
            System.out.print(" " + row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);

            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }

        printHeaders(isWhite);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void printHeaders(boolean isWhite) {
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_BOLD);

        System.out.print("   ");
//        String[] headers = { "\u2003a ", "\u2003b ", "\u2003c ", "\u2003d ", "\u2003e ", "\u2003f ", "\u2003g ", "\u2003h " };
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

        if (isWhite) {
            for (String h : headers) System.out.print(h);
        } else {
            for (int i = 7; i >= 0; i--) {
                System.out.print(headers[i]);
            }
        }

        System.out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println("   " + EscapeSequences.RESET_BG_COLOR);
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

//        switch (piece.getPieceType()) {
//            case KING -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
//            case QUEEN -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
//            case BISHOP -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
//            case KNIGHT -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
//            case ROOK -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
//            case PAWN -> System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
//        }
        System.out.print(EscapeSequences.SET_TEXT_BOLD);
        switch (piece.getPieceType()) {
            case KING -> System.out.print(" K ");
            case QUEEN -> System.out.print(" Q ");
            case BISHOP -> System.out.print(" B ");
            case KNIGHT -> System.out.print(" N ");
            case ROOK -> System.out.print(" R ");
            case PAWN -> System.out.print(" P ");
        }
        System.out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}