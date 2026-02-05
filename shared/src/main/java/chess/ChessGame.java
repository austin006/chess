package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Checks that a given move is valid
     *
     * @param move the move to check
     * @return true if move is valid
     */
    public boolean isValidMove(ChessMove move) {
        if (validMoves(move.getStartPosition()) == null) { return false; }
        return validMoves(move.getStartPosition()).contains(move);
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        for (ChessMove move: possibleMoves) {
            if(!leavesKingInCheck(move)) { // check if move leaves king in danger
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean leavesKingInCheck(ChessMove move) {
        ChessBoard tempBoard = new ChessBoard(this.board);
        applyMoveToBoard(move, tempBoard);

        ChessPiece piece = board.getPiece(move.getStartPosition());
        return isInCheck(piece.getTeamColor(), tempBoard);
    }

    private void temporaryMove(ChessMove move, ChessPiece piece) {
        if(move.getPromotionPiece() == null) {
            board.addPiece(move.getEndPosition(), piece);
        }
        else {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
        board.addPiece(move.getStartPosition(), null);
    }

    private void undoTemporaryMove(ChessMove move, ChessPiece startingPiece, ChessPiece capturedPiece) {
        board.addPiece(move.getStartPosition(), startingPiece);
        board.addPiece(move.getEndPosition(), capturedPiece);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if(piece != null && teamTurn == piece.getTeamColor() && isValidMove(move)) {
            applyMoveToBoard(move, board);
            setTeamTurn(teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        }
        else {
            throw new InvalidMoveException("Attempted to make an illegal move");
        }
    }

    private void applyMoveToBoard(ChessMove move, ChessBoard boardToUpdate) {
        ChessPiece piece = boardToUpdate.getPiece(move.getStartPosition());
        if(move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        boardToUpdate.addPiece(move.getStartPosition(), null);
        boardToUpdate.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard boardToCheck) {
        ChessPosition kingPosition = boardToCheck.getKingPosition(teamColor);
        // put all moves of opponent in a list
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = boardToCheck.getPiece(currentPosition);
                if(currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    // check if taking the king is an end position for any move
                    for(ChessMove move: currentPiece.pieceMoves(boardToCheck, currentPosition)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return !teamHasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !teamHasValidMoves(teamColor);
    }

    private boolean teamHasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if(currentPiece != null && currentPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(currentPosition);
                    if(moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Collection<ChessMove> getTeamValidMoves(TeamColor teamColor) {
        Collection<ChessMove> allValidMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if(currentPiece != null && currentPiece.getTeamColor() == teamColor) {
                    allValidMoves.addAll(validMoves(currentPosition));
                }
            }
        }
        return allValidMoves;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
