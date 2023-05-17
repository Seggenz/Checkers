import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import resources.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public  class Game {

    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8; //amount of bars
    public static final int HEIGHT = 8;
    protected boolean inCaptureSequence = false;
    protected Piece currentCapturePiece = null;
    protected Timeline timer;
    protected boolean gameOver = false;

    private StopWatch gameStopWatch = new StopWatch();
    private Label gameTimerLabel = new Label();
    private Timeline moveTimer;
    private Label moveTimerLabel = new Label();



    protected Tile[][] board = new Tile[WIDTH][HEIGHT];
    private Group tileGroup = new Group();
    protected Group pieceGroup = new Group();


    public Game() {
        this.gameStopWatch = new StopWatch();
    }
    public StopWatch getGameStopWatch() {
        return gameStopWatch;
    }

    public static PieceType currentPlayer = PieceType.WHITE;

    List<MoveResult> checkAvailableCaptures(PieceType currentPlayer) {
        List<MoveResult> availableCaptures = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && piece.getType() == currentPlayer || piece != null && piece.getType() == currentPlayer.getOppositeQueen() ) {
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
    boolean isCaptureAvailableForPiece(Piece piece) {
        if (piece == null) {
            return false;
        }
        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOldY());
        PieceType pieceType = piece.getType();
        boolean isQueen = pieceType == PieceType.RED_QUEEN || pieceType == PieceType.WHITE_QUEEN;

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

        startTimer();
        return root;
    }

protected MoveResult tryMove(Piece piece, int newX, int newY) {

    if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
        return new MoveResult(MoveType.NONE);
    }
    if (timer != null) {
        timer.stop();
        startTimer();
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
                if (board[x1][y1].getPiece().getType().getColor() == piece.getType().getColor()) {
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

        if(board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType().getColor() != piece.getType().getColor()) {
            if (inCaptureSequence && piece != currentCapturePiece) {
                return new MoveResult(MoveType.NONE);
            }

            return new MoveResult(MoveType.KILL, board[x1][y1].getPiece(), true, 1);
        }

    }

    return new MoveResult(MoveType.NONE);
}

    protected int toBoard(double pixel) { //returning piece position on tile
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }



    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {
            if (!gameStopWatch.isRunning()) {
                startGameTimer();
            }
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
                    checkGameOver();

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
                    checkGameOver();
                    break;
                }
            }
            startTimer();
        });



        return piece;

    }

    void checkGameOver() {
        int redPieces = 0;
        int whitePieces = 0;

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                Tile tile = board[x][y];
                if (tile.hasPiece()) {
                    PieceType type = tile.getPiece().getType();
                    if (type == PieceType.RED || type == PieceType.RED_QUEEN) {
                        redPieces++;
                    } else if (type == PieceType.WHITE || type == PieceType.WHITE_QUEEN) {
                        whitePieces++;
                    }
                }
            }
        }

        if (redPieces == 0) {
            gameOver = true;
            displayGameOverMessage("Białe wygrały!");
        } else if (whitePieces == 0) {
            gameOver = true;
            displayGameOverMessage("Czerwone wygrały!");
        }
    }

    private void displayGameOverMessage(String message) {
        stopGameTimer();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }



    public void makeRandomMove() {
        List<Piece> pieces = pieceGroup.getChildren().stream()
                .filter(p -> p instanceof Piece)
                .map(p -> (Piece) p)
                .filter(p -> p.getType().getColor() == currentPlayer.getColor())
                .collect(Collectors.toList());

        List<MoveResult> availableCaptures = checkAvailableCaptures(currentPlayer);
        boolean captureRequired = !availableCaptures.isEmpty();

        Random random = new Random();

        while (!pieces.isEmpty()) {
            Piece piece = pieces.get(random.nextInt(pieces.size()));

            List<MoveResult> validMoves = new ArrayList<>();
            List<Integer> validMovesNewX = new ArrayList<>();
            List<Integer> validMovesNewY = new ArrayList<>();
            for (int newX = 0; newX < WIDTH; newX++) {
                for (int newY = 0; newY < HEIGHT; newY++) {
                    MoveResult result = tryMove(piece, newX, newY);
                    if (result.getType() != MoveType.NONE && (!captureRequired || result.getType() == MoveType.KILL)) {
                        validMoves.add(result);
                        validMovesNewX.add(newX);
                        validMovesNewY.add(newY);
                    }
                }
            }

            if (!validMoves.isEmpty()) {
                int index = random.nextInt(validMoves.size());
                MoveResult chosenMove = validMoves.get(index);
                int newX = validMovesNewX.get(index);
                int newY = validMovesNewY.get(index);
                int x0 = toBoard(piece.getOldX());
                int y0 = toBoard(piece.getOldY());
                board[x0][y0].setPiece(null);  // Usunięcie pionka z poprzedniego pola
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);
                if (chosenMove.getType() == MoveType.KILL) {
                    Piece otherPiece = chosenMove.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    checkGameOver();
                    inCaptureSequence = true;
                    currentCapturePiece = piece;
                    if (!isCaptureAvailableForPiece(piece)) {
                        inCaptureSequence = false;
                        currentCapturePiece = null;
                        currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
                    }
                } else {
                    inCaptureSequence = false;
                    currentCapturePiece = null;
                    currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
                }
                break;
            } else {
                pieces.remove(piece);
            }

        }
    }


    public void startTimer() {
        if (moveTimer != null) {
            moveTimer.stop();
        }

        moveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            makeRandomMove();
            startTimer();
        }));

        moveTimer.setCycleCount(30); // Licznik będzie odliczał 30 sekund
        moveTimer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Aktualizacja etykiety zegara z nową wartością
            Platform.runLater(() -> moveTimerLabel.setText("Next move in: " + (30 - newValue.toSeconds()) + "s"));
        });

        moveTimer.playFromStart();
    }

    private void updateGameTimerLabel() {
        Platform.runLater(() -> gameTimerLabel.setText("Game time: " + gameStopWatch.getElapsedTime() / 1000 + "s"));
    }

    private void startGameTimer() {
        gameStopWatch.start();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateGameTimerLabel()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void stopGameTimer() {
        gameStopWatch.stop();
        updateGameTimerLabel();
    }




}
