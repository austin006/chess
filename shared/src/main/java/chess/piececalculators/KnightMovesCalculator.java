package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class KnightMovesCalculator extends StepPieceCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        int[][] reachableSpaces = {
                {row + 2, col + 1}, {row + 2, col - 1},
                {row - 2, col + 1}, {row - 2, col - 1},
                {row + 1, col + 2}, {row + 1, col - 2},
                {row - 1, col + 2}, {row - 1, col - 2}
        };

        return calculateStepMoves(board, position, reachableSpaces);
    }
}