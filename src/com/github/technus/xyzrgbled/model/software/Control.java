package com.github.technus.xyzrgbled.model.software;

import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import com.github.technus.xyzrgbled.model.hardware.Hardware;
import com.github.technus.xyzrgbled.model.hardware.PulseWidthModulationController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Control {
    private final SimpleBooleanProperty enable=new SimpleBooleanProperty();
    private final SimpleObjectProperty<ColorChooserXYZ> request =new SimpleObjectProperty<>();
    private final ObservableList<PIDz> regulators= FXCollections.observableArrayList();

    private final SimpleDoubleProperty regulatorGain=new SimpleDoubleProperty(.01D);
    private final SimpleDoubleProperty regulatorIntegrationTime=new SimpleDoubleProperty(.01);
    private final SimpleDoubleProperty regulatorDifferentiationTime=new SimpleDoubleProperty(0);
    private final SimpleBooleanProperty enableTransferFunctionRemoval=new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty enableOffsetRemoval=new SimpleBooleanProperty(true);

    public Control(Hardware hardware) {
        hardware.getDrivers().forEach(pulseWidthModulationController -> {
            PIDz pidz=new PIDz(pulseWidthModulationController,Control.this);
            pidz.setMax(PulseWidthModulationController.MAX_VALUE);
            pidz.setMin(PulseWidthModulationController.MIN_VALUE);
            pidz.enableProperty().bind(enable);
            pidz.regulatorGainProperty().bind(regulatorGain);
            pidz.integrationTimeProperty().bind(regulatorIntegrationTime);
            pidz.differentiationTimeProperty().bind(regulatorDifferentiationTime);
            pidz.enableInverseTransferCouplingProperty().bind(enableTransferFunctionRemoval);
            pidz.enableOffsetProperty().bind(enableOffsetRemoval);
            hardware.getColorSensor().colorProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue!=null) {
                    pidz.readingProperty().set(newValue);
                }
            });
            pidz.requestProperty().bind(request);
            regulators.add(pidz);
        });
    }

    public ColorChooserXYZ getRequest() {
        return request.get();
    }

    public SimpleObjectProperty<ColorChooserXYZ> requestProperty() {
        return request;
    }

    public void setRequest(ColorChooserXYZ request) {
        this.request.set(request);
    }

    public ObservableList<PIDz> getRegulators() {
        return regulators;
    }

    public boolean isEnable() {
        return enable.get();
    }

    public SimpleBooleanProperty enableProperty() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable.set(enable);
    }

    public double getRegulatorGain() {
        return regulatorGain.get();
    }

    public SimpleDoubleProperty regulatorGainProperty() {
        return regulatorGain;
    }

    public void setRegulatorGain(double regulatorGain) {
        this.regulatorGain.set(regulatorGain);
    }

    public double getRegulatorIntegrationTime() {
        return regulatorIntegrationTime.get();
    }

    public SimpleDoubleProperty regulatorIntegrationTimeProperty() {
        return regulatorIntegrationTime;
    }

    public void setRegulatorIntegrationTime(double regulatorIntegrationTime) {
        this.regulatorIntegrationTime.set(regulatorIntegrationTime);
    }

    public double getRegulatorDifferentiationTime() {
        return regulatorDifferentiationTime.get();
    }

    public SimpleDoubleProperty regulatorDifferentiationTimeProperty() {
        return regulatorDifferentiationTime;
    }

    public void setRegulatorDifferentiationTime(double regulatorDifferentiationTime) {
        this.regulatorDifferentiationTime.set(regulatorDifferentiationTime);
    }

    public boolean isEnableTransferFunctionRemoval() {
        return enableTransferFunctionRemoval.get();
    }

    public SimpleBooleanProperty enableTransferFunctionRemovalProperty() {
        return enableTransferFunctionRemoval;
    }

    public void setEnableTransferFunctionRemoval(boolean enableTransferFunctionRemoval) {
        this.enableTransferFunctionRemoval.set(enableTransferFunctionRemoval);
    }

    public boolean isEnableOffsetRemoval() {
        return enableOffsetRemoval.get();
    }

    public SimpleBooleanProperty enableOffsetRemovalProperty() {
        return enableOffsetRemoval;
    }

    public void setEnableOffsetRemoval(boolean enableOffsetRemoval) {
        this.enableOffsetRemoval.set(enableOffsetRemoval);
    }
}
