package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class RookMovesCalculator extends SlidingPieceCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
        return calculateLinearMoves(board, position, directions);
    }
}
