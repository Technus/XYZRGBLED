package com.github.technus.xyzrgbled;

import com.github.technus.xyzrgbled.javafx.mainWindow.MainWindowController;
import com.github.technus.xyzrgbled.model.software.Control;
import com.github.technus.xyzrgbled.model.hardware.Hardware;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Locale.setDefault(Locale.US);
        primaryStage.setTitle("XYZ RGB LED");
        Hardware hardware=new Hardware();
        primaryStage.setScene(new Scene(MainWindowController.start(hardware,new Control(hardware))));
        primaryStage.getScene().getStylesheets().add(Main.class.getResource("modena_dark.css").toExternalForm());
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
