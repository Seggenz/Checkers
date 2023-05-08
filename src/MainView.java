import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainView {
    private Stage stage;
    private BorderPane root;
    private Scene scene;

    private Game game;


    public MainView(Stage stage) {
        this.stage = stage;
        buildUI();
    }
    private void buildUI() {
        Button top = createButton("Top");
        Button left = createButton("Left");
        Button right = createButton("Right");
        Button bottom = createButton("Bottom");

        root = new BorderPane();
        game = new Game();
        root.setCenter(game.createContent());
        root.setTop(top);
        root.setLeft(left);
        root.setRight(right);
        root.setBottom(bottom);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("resources/css/style.css");
        stage.setResizable(false);
        stage.setTitle("Checkers");
        stage.setScene(scene);
        stage.show();

    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setMinWidth(150);

        BorderPane.setAlignment(button, Pos.CENTER);
        return button;
    }

}
