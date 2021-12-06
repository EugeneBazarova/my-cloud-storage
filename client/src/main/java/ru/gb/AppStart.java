package ru.gb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppStart extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/scene.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("[ My Java Cloud Storage ]");
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void start() {
        launch();
    }
}
