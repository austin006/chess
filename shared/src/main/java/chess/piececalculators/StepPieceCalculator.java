package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class StepPieceCalculator implements PieceMovesCalculator {

    protected Collection<ChessMove> calculateStepMoves(ChessBoard board, ChessPosition position, int[][] reachableSpaces) {
        List<ChessMove> moves = new ArrayList<>();

        for(int[] space : reachableSpaces) {
            if(space[0] >= 1 && space[0] <= 8 && space[1] >= 1 && space[1] <= 8) {

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