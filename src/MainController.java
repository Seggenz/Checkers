package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML //implementing values of all attributes from fxml file to controller file

    private Stage stage;
    private Scene scene;
    private Parent root;
    private String css = this.getClass().getResource("resources/css/style.css").toExternalForm();

    public void switchToPVEScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("resources/view/GamePVEScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToPVPScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("resources/view/GamePVPScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToStatisticsScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("resources/view/StatisticsScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToSettingsScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("resources/view/SettingsScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

}