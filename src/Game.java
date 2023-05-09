import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8; //amount of bars
    public static final int HEIGHT = 8;
    private boolean inCaptureSequence = false;
    private Piece currentCapturePiece = null;

    private Tile[][] board = new Tile[WIDTH][HEIGHT];
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    public static PieceType currentPlayer = PieceType.WHITE;

    private List<MoveResult> checkAvailableCaptures(PieceType currentPlayer) {
        List<MoveResult> availableCaptures = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && piece.getType() == currentPlayer ) {
                    for (int newY = 0; newY < HEIGHT; newY++) {
                        for (int newX = 0; newX < WIDTH; newX++) {
                            MoveResult result = tryMove(piece, newX, newY);
                            if (result.getType() == MoveType.KILL) {
                                availableCaptures.add(result);
                            }
                        }
                    }
                }
            }
        }

        return availableCaptures;
    }
    private boolean isCaptureAvailableForPiece(Piece piece) {
        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOldY());

        for (int newY = 0; newY < HEIGHT; newY++) {
            for (int newX = 0; newX < WIDTH; newX++) {
                MoveResult result = tryMove(piece, newX, newY);
                if (result.getType() == MoveType.KILL) {
                    return true;
                }
            }
        }

        return false;
    }

    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup,pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for(int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.RED, x , y);
                }
                if (y >= 5 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.WHITE, x , y);
                }

                if(piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }


            }
        }

        return root;
    }

private MoveResult tryMove(Piece piece, int newX, int newY) {
    if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
        return new MoveResult(MoveType.NONE);
    }

    int x0 = toBoard(piece.getOldX());
    int y0 = toBoard(piece.getOldY());

    boolean isQueen = piece.getType() == PieceType.RED_QUEEN || piece.getType() == PieceType.WHITE_QUEEN;

    if (!isQueen && Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().moveDir) {
        return new MoveResult(MoveType.NORMAL);
    }
    else if (isQueen && Math.abs(newX - x0) == Math.abs(newY - y0)) {
        int dx = newX > x0 ? 1 : -1;
        int dy = newY > y0 ? 1 : -1;
        int x1 = x0 + dx;
        int y1 = y0 + dy;
        boolean pathBlocked = false;
        boolean hasKill = false;
        Piece pieceToKill = null;

        while (x1 != newX || y1 != newY) {
            if (board[x1][y1].hasPiece()) {
                if (board[x1][y1].getPiece().getType() == piece.getType()) {
                    pathBlocked = true;
                    break;
                } else if (hasKill) {
                    pathBlocked = true;
                    break;
                } else {
                    hasKill = true;
                    pieceToKill = board[x1][y1].getPiece();
                }
            }

            x1 += dx;
            y1 += dy;
        }

        if (!pathBlocked) {
            if (hasKill) {
                return new MoveResult(MoveType.KILL, pieceToKill);
            } else {
                return new MoveResult(MoveType.NORMAL);
            }
        }
    }
    else if (!isQueen && Math.abs(newX - x0) == 2 && Math.abs(newY - y0) == 2) {
        int x1 = x0 + (newX - x0) / 2;
        int y1 = y0 + (newY - y0) / 2;

        if(board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
            if (inCaptureSequence && piece != currentCapturePiece) {
                return new MoveResult(MoveType.NONE);
            }
            return new MoveResult(MoveType.KILL, board[x1][y1].getPiece(), true);
        }
    }

    return new MoveResult(MoveType.NONE);
}

    private int toBoard(double pixel) { //returning piece position on tile
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }
    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());



            List<MoveResult> availableCaptures = checkAvailableCaptures(currentPlayer);
            boolean captureRequired = !availableCaptures.isEmpty();

            MoveResult result = tryMove(piece,newX,newY);
            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            if (captureRequired && result.getType() != MoveType.KILL) {
                piece.abortMove();
                return;
            }

            switch (result.getType()) {

                case NONE -> {
                    piece.abortMove();
                    break;
                }
                case NORMAL -> {
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
                    break;
                }
                case KILL -> {
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);

                    if (!isCaptureAvailableForPiece(piece)) {
                        currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
                    }
                    break;
                }
            }
        });

        return piece;

    }

}
