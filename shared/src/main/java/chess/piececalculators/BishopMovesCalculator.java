package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator extends SlidingPieceCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1, -1}};
        return calculateLinearMoves(board, position, directions);
    }
}
