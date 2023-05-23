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


    public interface OnMoveCompleteListener { //interfejs uzywany do obserwowania czy wykonano ruch
        void onMoveComplete();
    }

    public void setOnMoveCompleteListener(OnMoveCompleteListener listener) {
        this.moveCompleteListener = listener;
    }

    public Parent createContent() { //metoda sluzaca do stworzenia planszy
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
    List<MoveResult> checkAvailableCaptures(PieceType currentPlayer) { //sprawdzamy dostępne bicia dla wszystkich pionków naszej drużyny
        List<MoveResult> availableCaptures = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && piece.getType() == currentPlayer || piece != null && piece.getType() == currentPlayer.getQueen()) {
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

    boolean isCaptureAvailableForPiece(Piece piece) { //sprawdzamy czy dostępne jest bicie dla konkretnego piona
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

    protected MoveResult tryMove(Piece piece, int newX, int newY) { //sprawdzamy czy możliwy jest taki ruch

        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) { //jezeli pole ma piona lub nie jest pionem po skosie
            return new MoveResult(MoveType.NONE);
        }
        if (timer != null) { //pomocnicze pole do startowania licznika losowego ruchu
            timer.stop();
            startRandomMoveTimer();
        }

        int x0 = toBoard(piece.getOldX()); //poprzednie wspolrzedne figury
        int y0 = toBoard(piece.getOldY());

        boolean isQueen = piece.getType() == PieceType.RED_QUEEN || piece.getType() == PieceType.WHITE_QUEEN;

        if (!isQueen && Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().moveDir) { //nie jestesmy krolowa i jesli odleglosc miedzy stary mi i nowymi wspolrzednymi jest 1 i jest rowna moveDir
            if (moveCompleteListener != null) {
                moveCompleteListener.onMoveComplete();
            }
            return new MoveResult(MoveType.NORMAL);
        } else if (isQueen && Math.abs(newX - x0) == Math.abs(newY - y0)) { //czy jestesmy krolowa i poruszamy sie po skosie
            int dx = newX > x0 ? 1 : -1;
            int dy = newY > y0 ? 1 : -1;
            int x1 = x0 + dx;
            int y1 = y0 + dy;
            boolean pathBlocked = false; //zajete przez innego piona
            boolean hasKill = false; //czy musimy zbic piona
            Piece pieceToKill = null;

            while (x1 != newX || y1 != newY) { //iterujemy po skosnych polach i sprawdzamy czy musimy zbic krolowa
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
                    return new MoveResult(MoveType.KILL, pieceToKill); //zwracamy zbicie piona
                } else {
                    if (moveCompleteListener != null) {
                        moveCompleteListener.onMoveComplete();
                    }
                    return new MoveResult(MoveType.NORMAL); //zwracamy poruszenie sie piona
                }
            }
        } else if (!isQueen && Math.abs(newX - x0) == 2 && Math.abs(newY - y0) == 2) { //jesli nie jestesmy krolowa i chcemy zbic
            int x1 = x0 + (newX - x0) / 2; //pozycja figury ktora bedziemy zbijac
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType().getColor() != piece.getType().getColor()) { //czy zawiera figure o przeciwnym kolorze
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

    protected int toBoard(double pixel) { //przeksztalcanie wartosci pikselowej w pozycje figury na planszy
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE; //dodajemy polowe wartosci kafelka (aby miec jego srodek), nastepnie dzielimy przez wielkosc kafelka dzieki czemu uzyskujemy
                                                          //pozycje figury na planszy
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
            boolean captureRequired = !availableCaptures.isEmpty(); //jezeli mamy bicia to bicie jest obowiazkowe

            MoveResult result = tryMove(piece, newX, newY); //sprawdzamy ruch
            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            if (captureRequired && result.getType() != MoveType.KILL) { //jezeli musimy zbic a w tym ruchu nie zbilismy
                piece.abortMove(); //porzuc ruch
                return;
            }

            switch (result.getType()) {
                case NONE -> { //zabroniony ruch np na zle pole
                    piece.abortMove();
                    break;
                }
                case NORMAL -> { //przejscie na nowe pole
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
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
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null); //usuwamy z tablicy planszy zbity pionek
                    pieceGroup.getChildren().remove(otherPiece); //usuwamy zbity pionek z naszej grupy pinkow

                    if (!isCaptureAvailableForPiece(piece)) {
                        currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
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
    void getNumberOfPieces() { //funkcja pomocnicza do wyswietlania ilosci pionkow
        int tx = 0;
        int ty = 0;
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                Tile tile = board[x][y];
                if (tile.hasPiece()) {
                    PieceType type = tile.getPiece().getType();
                    if (type == PieceType.RED || type == PieceType.RED_QUEEN) {
                        tx++;
                    } else if (type == PieceType.WHITE || type == PieceType.WHITE_QUEEN) {
                        ty++;
                    }
                }
            }
        }
        redPieces = tx;
        whitePieces = ty;

    }

    List<MoveResult> checkAvailableMoves(PieceType currentPlayer) { //sprawdzamy mozliwe ruchy dla danego gracza
        List<MoveResult> availableMoves = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && piece.getType() == currentPlayer || piece != null && piece.getType() == currentPlayer.getQueen()) {
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

    void checkGameOver() { //sprawdzamy czy gra sie konczy
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

        if (redPieces == 0 || checkAvailableMoves(PieceType.RED).isEmpty()) { //jesli nie ma pionkow lub pionki nie moga sie poruszyc
            totalCapturedPieces = totalCapturedPieces + 14 + whitePieces;
            gameOver = true;
            displayGameOverMessage("Białe wygrały!");
            saveTotalCapturedPieces(totalCapturedPieces);
        } else if (whitePieces == 0 || checkAvailableMoves(PieceType.WHITE).isEmpty()) {
            totalCapturedPieces = totalCapturedPieces + 14 + redPieces;
            gameOver = true;
            displayGameOverMessage("Czerwone wygrały!");
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

            if (message.equals("Białe wygrały!")) { //wyswietlamy wiadomosc i zmieniamy statystyki
                whiteWins++;
                lines.set(2, String.valueOf(whiteWins));
            } else if (message.equals("Czerwone wygrały!")) {
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
        //tworzymy liste pionkow nalezacych do aktualnego gracza
        List<Piece> pieces = pieceGroup.getChildren().stream() //tworzymy z naszej grupy strumien
                .filter(p -> p instanceof Piece) //filtrujemy elementy strumienia
                .map(p -> (Piece) p) //mapujemy nasz element na klase Piece
                .filter(p -> p.getType().getColor() == currentPlayer.getColor())
                .collect(Collectors.toList()); //tworzymy liste z dostepnych figur

        List<MoveResult> availableCaptures = checkAvailableCaptures(currentPlayer);
        boolean captureRequired = !availableCaptures.isEmpty();

        Random random = new Random(); //tworzymy obiekt klasy random ktory przyda sie do generowania losowych wartosci

        while (!pieces.isEmpty()) {
            Piece piece = pieces.get(random.nextInt(pieces.size())); //losujemy losowa liczbe od 0 do wielkosci listy i przypisujemy ten pionek do naszego obiektu

            List<MoveResult> validMoves = new ArrayList<>(); //przechowujemy dostepne ruchy
            List<Integer> validMovesNewX = new ArrayList<>(); //i ich nowe wspolrzedne
            List<Integer> validMovesNewY = new ArrayList<>();
            for (int newX = 0; newX < WIDTH; newX++) {
                for (int newY = 0; newY < HEIGHT; newY++) {
                    MoveResult result = tryMove(piece, newX, newY);
                    if (result.getType() != MoveType.NONE && (!captureRequired || result.getType() == MoveType.KILL)) { //sprawdzamy czy czy jest mozliwe bicie
                        validMoves.add(result);
                        validMovesNewX.add(newX);
                        validMovesNewY.add(newY);
                    }
                }
            }

            if (!validMoves.isEmpty()) {
                int index = random.nextInt(validMoves.size());
                MoveResult chosenMove = validMoves.get(index); //losujemy ruch dla naszego pionka
                int newX = validMovesNewX.get(index);
                int newY = validMovesNewY.get(index);
                int x0 = toBoard(piece.getOldX());
                int y0 = toBoard(piece.getOldY());
                board[x0][y0].setPiece(null);  // usuwamy pionka z poprzedniego pola
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
                        currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
                    }
                } else {
                    inCaptureSequence = false;
                    currentCapturePiece = null;
                    currentPlayer = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
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

        long gameDuration = gameStopWatch.getElapsedTime() / 1000;  // dlugosc gry w sekundach
        long[] times = readGameTimesFromFile();

        if (gameDuration < times[0]) {
            times[0] = gameDuration;  // aktualizujemy najkrotszy czas gry
        }
        if (gameDuration > times[1]) {
            times[1] = gameDuration;  // aktualizujemy najdluzszy czas gry
        }

        // aktualizujemy czasy gry w pliku
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
                times[0] = convertToSeconds(lines.get(0));  // najkrotszy czas
                times[1] = convertToSeconds(lines.get(1));  // najdluzszy czas
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
            playerTurn = "Czerwony";
        } else if (currentPlayer == PieceType.WHITE) {
            playerTurn = "Biały";
        }
    }

    public PieceType getPlayerTurn() {
        return currentPlayer;
    }

}
