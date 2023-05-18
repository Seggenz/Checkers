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
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class MainView {

    @FXML

    private Stage stage;
    private BorderPane root;
    private Scene scene;
    private Game gamePVP;
    private ComputerGameEasy gamePVE;
    private Label GameTimeLabel;
    private Label pveGameTimeLabel;
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

    private void buildMenuUI() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("resources/view/Main.fxml"));
        scene = new Scene(root);
        scene.getStylesheets().add("resources/css/style.css");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    private void startPVPGame(ActionEvent event) {
        gamePVP = new Game();
        Node source = (Node) event.getSource();
        stage = (Stage) source.getScene().getWindow();
        buildGameUI(gamePVP);
    }
    @FXML
    private void startPVEGame(ActionEvent event) {
        gamePVE = new ComputerGameEasy();
        Node source = (Node) event.getSource();
        stage = (Stage) source.getScene().getWindow();
        buildGameUI(gamePVE);
    }

    private void buildGameUI(Game game) {
        root = new BorderPane();
        root.setCenter(game.createContent());

        // Tworzymy etykietę do wyświetlania aktualnego gracza
        currentPlayerLabel = new Label();
        currentPlayerLabel.setFont(new Font("Lucida Console", 24));
        updateCurrentPlayerLabel(game);

        VBox infoBox = new VBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(10); // Ustawiamy odstęp między elementami
        infoBox.getChildren().add(currentPlayerLabel);
        infoBox.setMinHeight(50);

        // Dodajemy VBox do góry BorderPane
        root.setTop(infoBox);

        VBox timeBox = new VBox();
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setSpacing(10); // Ustawiamy odstęp między licznikami

        // Tworzymy liczniki czasu
        GameTimeLabel = new Label("00:00"); // Tymczasowo ustawiamy tekst na "00:00"
        GameTimeLabel.setFont(new Font("Lucida Console", 24));

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

        timeBox.getChildren().add(GameTimeLabel);

        timeBox.setMinWidth(250);

        // Dodajemy VBox do lewej strony BorderPane
        root.setLeft(timeBox);

        whitePiecesLabel = new Label();
        whitePiecesLabel.setFont(new Font("Lucida Console", 24));
        redPiecesLabel = new Label();
        redPiecesLabel.setFont(new Font("Lucida Console", 24));

        VBox piecesBox = new VBox();
        piecesBox.setAlignment(Pos.CENTER);
        piecesBox.setSpacing(10); // Ustawiamy odstęp między elementami
        piecesBox.getChildren().addAll(whitePiecesLabel, redPiecesLabel);
        piecesBox.setMinWidth(250);

        // Dodajemy VBox do prawej strony BorderPane
        root.setRight(piecesBox);

        Timeline piecesTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            game.getNumberOfPieces();
            updatePiecesLabels(game);
        }));
        piecesTimeline.setCycleCount(Timeline.INDEFINITE);
        piecesTimeline.play();

        Scene gameScene = new Scene(root);
        gameScene.getStylesheets().add("resources/css/style.css");

        AnchorPane bottom = new AnchorPane();
        bottom.setMinHeight(50);
        root.setBottom(bottom);

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

    private void updatePvpGameTimeLabel() {
        if (gamePVP != null && GameTimeLabel != null) {
            GameTimeLabel.setText(gamePVP.getGameStopWatch().toString());
        }
    }

    private void updatePveGameTimeLabel() {
        if (gamePVE != null && pveGameTimeLabel != null) {
            pveGameTimeLabel.setText(gamePVE.getGameStopWatch().toString());
        }
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
