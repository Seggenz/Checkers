import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


public class Game {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8; //amount of bars
    public static final int HEIGHT = 8;
    protected boolean inCaptureSequence = false;
    protected Piece currentCapturePiece = null;
    protected Timeline timer;
    protected boolean gameOver = false;
    private int totalCapturedPieces = 0;
    private StopWatch gameStopWatch = new StopWatch();
    private final Label gameTimerLabel = new Label();
    private Timeline moveTimer;
    private final Label moveTimerLabel = new Label();
    public static String playerTurn = "Biały";
    File gameTimesFile = new File("src/resources/files/stats.txt");
    protected Tile[][] board = new Tile[WIDTH][HEIGHT];
    private final Group tileGroup = new Group();
    protected Group pieceGroup = new Group();
    public static PieceType currentPlayer = PieceType.WHITE;
    int redPieces = 0;
    int whitePieces = 0;
    public MainView mainView;
    private OnMoveCompleteListener moveCompleteListener;
    public Game() {
        this.gameStopWatch = new StopWatch();
    }

    public Game(MainView mainView) {
        this.mainView = mainView;
    }

    public StopWatch getGameStopWatch() {
        return gameStopWatch;
    }


    public interface OnMoveCompleteListener {// interface used for observing whether a move is completed
        void onMoveComplete();
    }

    public void setOnMoveCompleteListener(OnMoveCompleteListener listener) {
        this.moveCompleteListener = listener;
    }

    public Parent createContent() { // method for creating the game board
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.RED, x, y);
                }
                if (y >= 5 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }

                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }


            }
        }

        startRandomMoveTimer();
        return root;
    }
    List<MoveResult> checkAvailableCaptures(PieceType currentPlayer) { // checking available captures for all pieces of our team
        List<MoveResult> availableCaptures = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && (piece.getType() == currentPlayer || (piece != null && piece.getType() == currentPlayer.getQueen()))) {
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

    boolean isCaptureAvailableForPiece(Piece piece) { // checking if there is a capture available for a specific piece
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

    protected MoveResult tryMove(Piece piece, int newX, int newY) { // checking if such a move is possible

        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) { // if the square has a piece or is not a diagonal square
            return new MoveResult(MoveType.NONE);
        }
        if (timer != null) { // helper field to start the random move timer
            timer.stop();
            startRandomMoveTimer();
        }

        int x0 = toBoard(piece.getOldX()); // previous coordinates of the piece
        int y0 = toBoard(piece.getOldY());

        boolean isQueen = piece.getType() == PieceType.RED_QUEEN || piece.getType() == PieceType.WHITE_QUEEN;

        if (!isQueen && Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().moveDir) { // if we are not a queen and the distance between old and new coordinates is 1 and equals moveDir
            if (moveCompleteListener != null) {
                moveCompleteListener.onMoveComplete();
            }
            return new MoveResult(MoveType.NORMAL);
        } else if (isQueen && Math.abs(newX - x0) == Math.abs(newY - y0)) { // if we are a queen and move diagonally
            int dx = newX > x0 ? 1 : -1;
            int dy = newY > y0 ? 1 : -1;
            int x1 = x0 + dx;
            int y1 = y0 + dy;
            boolean pathBlocked = false; // occupied by another piece
            boolean hasKill = false; // whether we need to capture a piece
            Piece pieceToKill = null;

            while (x1 != newX || y1 != newY) { // iterate over diagonal squares and check if we need to capture a queen
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
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    return new MoveResult(MoveType.KILL, pieceToKill); // return capturing the piece
                } else {
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    return new MoveResult(MoveType.NORMAL); // return moving the piece
                }
            }
        } else if (!isQueen && Math.abs(newX - x0) == 2 && Math.abs(newY - y0) == 2) { // if we are not a queen and want to capture
            int x1 = x0 + (newX - x0) / 2; // position of the piece we will capture
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType().getColor() != piece.getType().getColor()) { // if it contains a piece of a different color
                if (inCaptureSequence && piece != currentCapturePiece) {
                    return new MoveResult(MoveType.NONE);
                }
                if (moveCompleteListener != null) {
                    moveCompleteListener.onMoveComplete();
                }
                return new MoveResult(MoveType.KILL, board[x1][y1].getPiece(), true, 1);
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    protected int toBoard(double pixel) { // converting pixel value to piece position on the board
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE; // add half the tile value (to have its center), then divide by the tile size to get the piece position on the board
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
            boolean captureRequired = !availableCaptures.isEmpty(); // if captures are available, capturing is mandatory

            MoveResult result = tryMove(piece, newX, newY); // check the move
            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            if (captureRequired && result.getType() != MoveType.KILL) { // if we need to capture and didn't capture in this move
                piece.abortMove(); // discard the move
                return;
            }

            switch (result.getType()) {
                case NONE -> { // forbidden move, e.g., to an invalid square
                    piece.abortMove();
                    break;
                }
                case NORMAL -> { // move to a new square
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    currentPlayer = (currentPlayer == PieceType.RED) ? PieceType.WHITE : PieceType.RED;
                    updatePlayerTurnLabel();
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    checkGameOver();
                    break;
                }
                case KILL -> {
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null); // remove the captured piece from the board array
                    pieceGroup.getChildren().remove(otherPiece); // remove the captured piece from our piece group

                    if (!isCaptureAvailableForPiece(piece)) {
                        currentPlayer = (currentPlayer == PieceType.RED) ? PieceType.WHITE : PieceType.RED;
                    }
                    updatePlayerTurnLabel();
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    checkGameOver();
                    break;
                }
            }
            startRandomMoveTimer();
        });

        return piece;
    }

    void getNumberOfPieces() { // helper function to display the number of pieces
        int redCount = 0;
        int whiteCount = 0;
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                Tile tile = board[x][y];
                if (tile.hasPiece()) {
                    PieceType type = tile.getPiece().getType();
                    if (type == PieceType.RED || type == PieceType.RED_QUEEN) {
                        redCount++;
                    } else if (type == PieceType.WHITE || type == PieceType.WHITE_QUEEN) {
                        whiteCount++;
                    }
                }
            }
        }
        redPieces = redCount;
        whitePieces = whiteCount;
    }

    List<MoveResult> checkAvailableMoves(PieceType currentPlayer) { // check possible moves for a given player
        List<MoveResult> availableMoves = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && (piece.getType() == currentPlayer || (piece != null && piece.getType() == currentPlayer.getQueen()))) {
                    for (int newY = 0; newY < HEIGHT; newY++) {
                        for (int newX = 0; newX < WIDTH; newX++) {
                            MoveResult result = tryMove(piece, newX, newY);
                            if (result.getType() == MoveType.NORMAL || result.getType() == MoveType.KILL) {
                                availableMoves.add(result);
                            }
                        }
                    }
                }
            }
        }

        return availableMoves;
    }

    void checkGameOver() { // check if the game is over
        int redCount = 0;
        int whiteCount = 0;

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                Tile tile = board[x][y];
                if (tile.hasPiece()) {
                    PieceType type = tile.getPiece().getType();
                    if (type == PieceType.RED || type == PieceType.RED_QUEEN) {
                        redCount++;
                    } else if (type == PieceType.WHITE || type == PieceType.WHITE_QUEEN) {
                        whiteCount++;
                    }
                }
            }
        }

        if (redCount == 0 || checkAvailableMoves(PieceType.RED).isEmpty()) { // if there are no red pieces or red pieces cannot move
            totalCapturedPieces += 14 + whiteCount;
            gameOver = true;
            displayGameOverMessage("White wins!");
            saveTotalCapturedPieces(totalCapturedPieces);
        } else if (whiteCount == 0 || checkAvailableMoves(PieceType.WHITE).isEmpty()) {
            totalCapturedPieces += 14 + redCount;
            gameOver = true;
            displayGameOverMessage("Red wins!");
            saveTotalCapturedPieces(totalCapturedPieces);
        }
    }
    private void displayGameOverMessage(String message) {
        stopGameTimer();
        try {
            Path path = Paths.get(gameTimesFile.toURI());
            List<String> lines = Files.readAllLines(path);
            int whiteWins = Integer.parseInt(lines.get(2));
            int redWins = Integer.parseInt(lines.get(3));

            if (message.equals("White wins!")) {
                whiteWins++;
                lines.set(2, String.valueOf(whiteWins));
            } else if (message.equals("Red wins!")) {
                redWins++;
                lines.set(3, String.valueOf(redWins));
            }

            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message);

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(okButtonType);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == okButtonType) {
                try {
                    mainView.buildMenuUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
                board[x0][y0].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);
                if (chosenMove.getType() == MoveType.KILL) {
                    Piece otherPiece = chosenMove.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    checkGameOver();
                    inCaptureSequence = true;
                    currentCapturePiece = piece;
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    if (!isCaptureAvailableForPiece(piece)) {
                        inCaptureSequence = false;
                        currentCapturePiece = null;
                        currentPlayer = (currentPlayer == PieceType.RED) ? PieceType.WHITE : PieceType.RED;
                    }
                } else {
                    inCaptureSequence = false;
                    currentCapturePiece = null;
                    currentPlayer = (currentPlayer == PieceType.RED) ? PieceType.WHITE : PieceType.RED;
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                }
                break;
            } else {
                pieces.remove(piece);
            }

        }
    }

    public void startRandomMoveTimer() {
        if (moveTimer != null) {
            moveTimer.stop();
        }

        moveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            makeRandomMove();
            startRandomMoveTimer();
        }));

        moveTimer.setCycleCount(30);
        moveTimer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
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

        long gameDuration = gameStopWatch.getElapsedTime() / 1000;
        long[] times = readGameTimesFromFile();

        if (gameDuration < times[0]) {
            times[0] = gameDuration;
        }
        if (gameDuration > times[1]) {
            times[1] = gameDuration;
        }

        try {
            Path path = Paths.get(gameTimesFile.toURI());
            List<String> lines = Files.readAllLines(path);
            if (lines.size() >= 2) {
                lines.set(0, String.format("%02d:%02d", times[0] / 60, times[0] % 60));
                lines.set(1, String.format("%02d:%02d", times[1] / 60, times[1] % 60));
                Files.write(path, lines);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long[] readGameTimesFromFile() {
        try {
            Path path = Paths.get(gameTimesFile.toURI());
            List<String> lines = Files.readAllLines(path);

            if (lines.size() >= 2) {
                long[] times = new long[2];
                times[0] = convertToSeconds(lines.get(0));
                times[1] = convertToSeconds(lines.get(1));
                return times;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new long[]{Long.MAX_VALUE, 0};
    }

    private void saveTotalCapturedPieces(int capturedPieces) {
        try {
            Path path = Paths.get(gameTimesFile.toURI());
            List<String> lines = Files.readAllLines(path);

            if (lines.size() >= 5) {
                int totalCapturedPieces = Integer.parseInt(lines.get(4));
                totalCapturedPieces += capturedPieces;
                lines.set(4, String.valueOf(totalCapturedPieces));
                Files.write(path, lines);
            } else {
                lines.add(String.valueOf(capturedPieces));
                Files.write(path, lines, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long convertToSeconds(String time) {
        String[] parts = time.split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return minutes * 60 + seconds;
    }


    private void updatePlayerTurnLabel() {
        if (currentPlayer == PieceType.RED) {
            playerTurn = "Red";
        } else if (currentPlayer == PieceType.WHITE) {
            playerTurn = "White";
        }
    }

    public PieceType getPlayerTurn() {
        return currentPlayer;
    }

}
