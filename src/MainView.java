import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import resources.StopWatch;

import java.io.IOException;
import java.util.Objects;

public class MainView {

    @FXML

    private Stage stage;
    private BorderPane root;
    private Scene scene;
    private Game gamePVP;
    private ComputerGameEasy gamePVE;
    private Label pvpGameTimeLabel;
    private Label pveGameTimeLabel;
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

        StopWatch gameStopWatch = game.getGameStopWatch();
        gameStopWatch.start();

        VBox timeBox = new VBox();
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setSpacing(10); // Ustawiamy odstęp między licznikami

        // Tworzymy liczniki czasu
        if (game instanceof Game) {
            pvpGameTimeLabel = new Label("00:00"); // Tymczasowo ustawiamy tekst na "00:00"
            pvpGameTimeLabel.setFont(new Font("Lucida Console", 24));

            // Używamy Timeline do cyklicznego aktualizowania tekstu etykiety
            Timeline pvpTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                long elapsedTime = gameStopWatch.getElapsedTime();
                pvpGameTimeLabel.setText(formatElapsedTime(elapsedTime));
            }));
            pvpTimeline.setCycleCount(Timeline.INDEFINITE);
            pvpTimeline.play();

            timeBox.getChildren().add(pvpGameTimeLabel);
        } else if (game instanceof ComputerGameEasy) {
            pveGameTimeLabel = new Label("00:00"); // Tymczasowo ustawiamy tekst na "00:00"
            pveGameTimeLabel.setFont(new Font("Lucida Console", 24));

            // Używamy Timeline do cyklicznego aktualizowania tekstu etykiety
            Timeline pveTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                long elapsedTime = gameStopWatch.getElapsedTime();
                pveGameTimeLabel.setText(formatElapsedTime(elapsedTime));
            }));
            pveTimeline.setCycleCount(Timeline.INDEFINITE);
            pveTimeline.play();

            timeBox.getChildren().add(pveGameTimeLabel);
        }

        // Dodajemy VBox do lewej strony BorderPane
        root.setLeft(timeBox);

        Scene gameScene = new Scene(root);
        gameScene.getStylesheets().add("resources/css/style.css");

        stage.setResizable(false);
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
        if (gamePVP != null && pvpGameTimeLabel != null) {
            pvpGameTimeLabel.setText(gamePVP.getGameStopWatch().toString());
        }
    }

    private void updatePveGameTimeLabel() {
        if (gamePVE != null && pveGameTimeLabel != null) {
            pveGameTimeLabel.setText(gamePVE.getGameStopWatch().toString());
        }
    }
}
