package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator{

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1, -1}};

        for(int[] direction : directions) {
            int row = position.getRow();
            int col = position.getColumn();

            while(true) {
                row += direction[0];
                col += direction[1];

                if(row > 8 || row < 1 || col < 1 || col > 8) break;

                ChessPosition potentialSpace = new ChessPosition(row, col);
                ChessPiece pieceAtSpace = board.getPiece(potentialSpace);

                if(pieceAtSpace == null) {
                    moves.add(new ChessMove(position, potentialSpace, null));
                }
                else {
                    if(pieceAtSpace.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, potentialSpace, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
