package com.github.technus.xyzrgbled.model.hardware;

import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import jssc.SerialPort;

public class Hardware {
    private final SerialPortProperty port=new SerialPortProperty();
    private final SimpleObjectProperty<ColorSensor> colorSensor=new SimpleObjectProperty<>(new ColorSensor(this));
    private final ObservableList<PulseWidthModulationController> drivers= FXCollections.observableArrayList();

    public Hardware() {
        drivers.addAll(
                new PulseWidthModulationController(this,4,new ColorLedXYZ(Color.RED)),
                new PulseWidthModulationController(this,6,new ColorLedXYZ(Color.GREEN)),
                new PulseWidthModulationController(this,7,new ColorLedXYZ(Color.BLUE)),
                new PulseWidthModulationController(this,5,new ColorLedXYZ(Color.WHITE)));
    }

    public ColorSensor getColorSensor() {
        return colorSensor.get();
    }

    public SimpleObjectProperty<ColorSensor> colorSensorProperty() {
        return colorSensor;
    }

    public void setColorSensor(ColorSensor colorSensor) {
        this.colorSensor.set(colorSensor);
    }

    public ObservableList<PulseWidthModulationController> getDrivers() {
        return drivers;
    }

    public SerialPort getPort() {
        return port.get();
    }

    public SerialPortProperty portProperty() {
        return port;
    }

    public void setPort(SerialPort port) {
        this.port.set(port);
    }
}
