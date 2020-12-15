package com.github.technus.xyzrgbled.model.hardware;

import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class Hardware {
    private final SimpleObjectProperty<IHardwareCommunication> communication =new SimpleObjectProperty<>(new SerialPortProperty());
    private final SimpleObjectProperty<ColorSensor> colorSensor=new SimpleObjectProperty<>(new ColorSensor(this));
    private final ObservableList<PulseWidthModulationController> drivers= FXCollections.observableArrayList();

    public static Hardware create(){
        return new Hardware();
    }

    public static Hardware createWithDrivers(){
        Hardware hardware = new Hardware();
        hardware.getDrivers().addAll(
                new PulseWidthModulationController(hardware,8,new ColorLedXYZ(Color.BLACK)),
                new PulseWidthModulationController(hardware,4,new ColorLedXYZ(Color.RED)  ),
                new PulseWidthModulationController(hardware,6,new ColorLedXYZ(Color.GREEN)),
                new PulseWidthModulationController(hardware,7,new ColorLedXYZ(Color.BLUE) ),
                new PulseWidthModulationController(hardware,5,new ColorLedXYZ(Color.WHITE)));
        return hardware;
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

    public IHardwareCommunication getCommunication() {
        return communication.get();
    }

    public SimpleObjectProperty<IHardwareCommunication> communicationProperty() {
        return communication;
    }

    public void setCommunication(IHardwareCommunication communication) {
        this.communication.set(communication);
    }
}
