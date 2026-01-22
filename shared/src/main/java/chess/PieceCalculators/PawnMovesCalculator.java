package chess.PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int row = position.getRow();
        int col = position.getColumn();
        boolean isWhite = board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE;

        int rowDirection = -1;
        int startRow = 7;
        if (isWhite) {
            rowDirection = 1;
            startRow = 2;
        }
        int nextRow = row + rowDirection;

        if(isOnBoard(nextRow, col)) {
            ChessPosition potentialSpace = new ChessPosition(nextRow, col);
            ChessPiece pieceAtSpace = board.getPiece(potentialSpace);

            if(pieceAtSpace == null){
                addPawnMove(moves, position, potentialSpace, isWhite);

                if(row == startRow && board.getPiece((new ChessPosition(nextRow + rowDirection, col))) == null) {
                    moves.add(new ChessMove(position, new ChessPosition(nextRow + rowDirection, col), null));
                }
            }
        }

        int[] sideCols = {col - 1, col + 1};
        for (int sideCol : sideCols) {
            if (isOnBoard(nextRow, sideCol)) {
                ChessPosition potentialSpace = new ChessPosition(nextRow, sideCol);
                ChessPiece pieceAtSpace = board.getPiece(potentialSpace);

                if (pieceAtSpace != null && pieceAtSpace.getTeamColor() != board.getPiece(position).getTeamColor()) {
                    addPawnMove(moves, position, potentialSpace, isWhite);
                }
            }
        }

        return moves;
    }

    private boolean isOnBoard(int row, int col) {
        return row <= 8 && row >= 1 && col >= 1 && col <= 8;
    }

    private void addPawnMove(List<ChessMove> moves, ChessPosition start, ChessPosition end, boolean isWhite) {
        int promotionRow = 1;
        if (isWhite) {
            promotionRow = 8;
        }
        if(end.getRow() == promotionRow) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
