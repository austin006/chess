package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int row = position.getRow();
        int col = position.getColumn();

        int[][] reachableSpaces = {
                {row + 1, col - 1}, {row + 1, col}, {row + 1, col + 1},
                {row, col - 1}, {row, col + 1},
                {row - 1, col - 1}, {row - 1, col}, {row - 1, col + 1}
        };

        for(int[] space : reachableSpaces) {
                if(!(space[0] > 8 || space[0] < 1 || space[1] < 1 || space[1] > 8)) {
                    ChessPosition potentialSpace = new ChessPosition(space[0], space[1]);
                    ChessPiece pieceAtSpace = board.getPiece(potentialSpace);

                    if (pieceAtSpace == null) {
                        moves.add(new ChessMove(position, potentialSpace, null));
                    } else {
                        if (pieceAtSpace.getTeamColor() != board.getPiece(position).getTeamColor()) {
                            moves.add(new ChessMove(position, potentialSpace, null));
                        }
                    }
                }
            }

        return moves;
    }
}
