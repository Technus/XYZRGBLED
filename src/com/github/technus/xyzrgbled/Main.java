package com.github.technus.xyzrgbled;

import com.github.technus.xyzrgbled.javafx.mainWindow.MainWindowController;
import com.github.technus.xyzrgbled.model.hardware.*;
import com.github.technus.xyzrgbled.model.software.Control;
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
        IHardwareCommunication.suppliers.add(SerialPortProperty::updatePorts);
        IHardwareCommunication.suppliers.add(SSHPortProperty::generateConnection);
        IHardwareCommunication.suppliers.add(ServerSocketPortProperty::generateConnection);
        IHardwareCommunication.suppliers.add(SocketPortProperty::generateConnection);

        Locale.setDefault(Locale.US);
        primaryStage.setTitle("XYZ RGB LED");
        Hardware hardware=Hardware.createWithDrivers();
        primaryStage.setScene(new Scene(MainWindowController.start(hardware,Control.getPIDzControl(hardware))));
        primaryStage.getScene().getStylesheets().add(Main.class.getResource("modena_dark.css").toExternalForm());
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
