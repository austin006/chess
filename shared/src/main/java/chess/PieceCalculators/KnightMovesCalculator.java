package chess.PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int row = position.getRow();
        int col = position.getColumn();

        int[][] reachableSpaces = {
                {row + 2, col + 1}, {row + 2, col - 1},
                {row - 2, col + 1}, {row - 2, col - 1},
                {row + 1, col + 2}, {row + 1, col - 2},
                {row - 1, col + 2}, {row - 1, col - 2}
        };

        for(int[] space : reachableSpaces) {
            if(!(space[0] > 8 || space[0] < 1 || space[1] < 1 || space[1] > 8)) {
                ChessPosition potentialSpace = new ChessPosition(space[0], space[1]);
                ChessPiece pieceAtSpace = board.getPiece(potentialSpace);

                if (pieceAtSpace == null || pieceAtSpace.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, potentialSpace, null));
                }
            }
        }
        return moves;
    }
}
