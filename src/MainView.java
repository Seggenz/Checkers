import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MainView {

    @FXML

    private Stage stage;
    public BorderPane root;
    private Scene scene;
    private Game gamePVP;
    private ComputerGameEasy gamePVE;
    private Label GameTimeLabel;
    private Label currentPlayerLabel;
    private Label whitePiecesLabel;
    private Label redPiecesLabel;

    public MainView() {

    }
    public MainView(Stage stage) throws IOException {
        this.stage = stage;
        try {
            buildMenuUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildMenuUI() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("resources/view/Main.fxml"));
        scene = new Scene(root);
        scene.getStylesheets().add("resources/css/style.css");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    private void startPVPGame(ActionEvent event) {
        gamePVP = new Game(this);
        Node source = (Node) event.getSource();
        stage = (Stage) source.getScene().getWindow();
        buildGameUI(gamePVP);
    }
    @FXML
    private void startPVEGame(ActionEvent event) {
        gamePVE = new ComputerGameEasy(this);
        Node source = (Node) event.getSource();
        stage = (Stage) source.getScene().getWindow();
        buildGameUI(gamePVE);
    }
    @FXML
    private void buildStatistics(ActionEvent event) {
        List<String> stats = new ArrayList<>();
        try {
            Path path = Paths.get("src/resources/files/stats.txt");
            stats = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Utwórz nowe okno
        Stage statisticsStage = new Stage();

        // Utwórz etykiety do wyświetlania statystyk
        Label longestGameLabel = new Label("Najkrótszy mecz: " + stats.get(0));
        Label shortestGameLabel = new Label("Najdłuższy mecz: " + stats.get(1));
        Label redWinsLabel = new Label("Liczba wygranych meczy przez białą drużynę: " + stats.get(2));
        Label whiteWinsLabel = new Label("Liczba wygranych meczy przez czerwoną drużynę: " + stats.get(3));
        Label totalPiecesTakenLabel = new Label("Liczba sumarycznie zbitych pionów: " + stats.get(4));


        // Dodaj etykiety do kontenera VBox
        VBox statisticsVBox = new VBox(
                longestGameLabel,
                shortestGameLabel,
                redWinsLabel,
                whiteWinsLabel,
                totalPiecesTakenLabel
        );
        statisticsVBox.setSpacing(10);

        // Utwórz nową scenę
        Scene scene = new Scene(statisticsVBox, 800, 200);
        scene.getStylesheets().add("resources/css/style.css");
        // Ustaw scenę dla okna i wyświetl okno
        statisticsStage.setScene(scene);
        statisticsStage.setTitle("Statystyki");
        statisticsStage.show();
    }

    private void buildGameUI(Game game) {
        // Utworzenie komponentów interfejsu użytkownika
        currentPlayerLabel = new Label();
        currentPlayerLabel.setFont(new Font("Lucida Console", 24));
        whitePiecesLabel = new Label();
        whitePiecesLabel.setFont(new Font("Lucida Console", 24));
        redPiecesLabel = new Label();
        redPiecesLabel.setFont(new Font("Lucida Console", 24));
        GameTimeLabel = new Label("00:00");
        GameTimeLabel.setFont(new Font("Lucida Console", 24));

        VBox infoBox = new VBox(currentPlayerLabel);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(10);
        infoBox.setMinHeight(50);

        VBox timeBox = new VBox(GameTimeLabel);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setSpacing(10);
        timeBox.setMinWidth(250);

        VBox piecesBox = new VBox(whitePiecesLabel, redPiecesLabel);
        piecesBox.setAlignment(Pos.CENTER);
        piecesBox.setSpacing(10);
        piecesBox.setMinWidth(250);

        AnchorPane bottom = new AnchorPane();
        bottom.setMinHeight(50);

        // Konfiguracja głównego panelu
        root = new BorderPane();
        root.setCenter(game.createContent());
        root.setTop(infoBox);
        root.setLeft(timeBox);
        root.setRight(piecesBox);
        root.setBottom(bottom);


        // Aktualizacja interfejsu użytkownika
        updateCurrentPlayerLabel(game);

        // Uruchamianie zegarów
        StopWatch gameStopWatch = game.getGameStopWatch();
        gameStopWatch.start();

        AtomicReference<Timeline> pvpTimelineRef = new AtomicReference<>();
        pvpTimelineRef.set(new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long elapsedTime = gameStopWatch.getElapsedTime();
            GameTimeLabel.setText(formatElapsedTime(elapsedTime));

            if (game.gameOver) {
                pvpTimelineRef.get().stop();
            }
        })));

        Timeline currentPlayerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            updateCurrentPlayerLabel(game);
        }));
        currentPlayerTimeline.setCycleCount(Timeline.INDEFINITE);
        currentPlayerTimeline.play();

        Timeline pvpTimeline = pvpTimelineRef.get();
        pvpTimeline.setCycleCount(Timeline.INDEFINITE);
        pvpTimeline.play();

        Timeline piecesTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            game.getNumberOfPieces();
            updatePiecesLabels(game);
        }));
        piecesTimeline.setCycleCount(Timeline.INDEFINITE);
        piecesTimeline.play();

        // Konfiguracja i wyświetlanie sceny
        Scene gameScene = new Scene(root);
        gameScene.getStylesheets().add("resources/css/style.css");

        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Wyjście do menu");
                alert.setHeaderText("Czy jesteś pewny, że chcesz wrócić do Menu?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        buildMenuUI();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        root.requestFocus();

        stage.setResizable(true);
        stage.setTitle("Checkers");
        stage.setScene(gameScene);
        stage.show();
    }

    // Ta metoda konwertuje czas w milisekundach na format MM:SS
    private String formatElapsedTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void updateCurrentPlayerLabel(Game game) {
        // Zakładamy, że metoda getPlayerTurn() zwraca aktualnego gracza
        String currentPlayer = game.getPlayerTurn().toString();
        currentPlayerLabel.setText("Aktualny gracz: " + currentPlayer);
    }

    public void updatePiecesLabels(Game game) {
        // Zakładamy, że metody getNumberOfWhitePieces() i getNumberOfBlackPieces() zwracają liczbę białych i czarnych pionków
        int whitePieces = game.whitePieces;
        int redPieces = game.redPieces;

        Platform.runLater(() -> {
            whitePiecesLabel.setText("Białe pionki: " + whitePieces);
            redPiecesLabel.setText("Czerwone pionki: " + redPieces);
        });
    }
}
