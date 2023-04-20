import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        StackPane layout = new StackPane();

        Button button = new Button("Hello World");

        button.setOnAction(actionEvent ->  {
            System.out.println(System.getProperty("java.version"));
        });

        layout.getChildren().add(button);
        Scene scene = new Scene(layout, 300, 300);
        stage.setScene(scene);
        stage.setTitle("JavaFX 20");
        stage.show();
    }
}