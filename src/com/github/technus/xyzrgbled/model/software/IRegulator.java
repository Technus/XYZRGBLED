package com.github.technus.xyzrgbled.model.software;

import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import com.github.technus.xyzrgbled.model.color.ColorReadingXYZ;
import com.github.technus.xyzrgbled.model.hardware.PulseWidthModulationController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public interface IRegulator {

    default void setPwm(PulseWidthModulationController pwm) {
        pwmProperty().set(pwm);
    }
    SimpleObjectProperty<PulseWidthModulationController> pwmProperty();
    default PulseWidthModulationController getPwm(){
        return pwmProperty().get();
    }
    default double getMax() {
        return maxProperty().get();
    }

    SimpleDoubleProperty maxProperty();

    default void setMax(double max) {
        maxProperty().set(max);
    }

    default double getMin() {
        return minProperty().get();
    }

    SimpleDoubleProperty minProperty();

    default void setMin(double min) {
        minProperty().set(min);
    }

    default double getSetting() {
        return settingProperty().get();
    }

    SimpleDoubleProperty settingProperty();

    default void setSetting(double setting) {
        settingProperty().set(setting);
    }

    double getNextSetting(double reading, double requestedValue);

    default ColorReadingXYZ getReading() {
        return readingProperty().get();
    }

    SimpleObjectProperty<ColorReadingXYZ> readingProperty();

    default void setReading(ColorReadingXYZ reading) {
        readingProperty().set(reading);
    }

    default boolean isEnable() {
        return enableProperty().get();
    }

    SimpleBooleanProperty enableProperty();

    default void setEnable(boolean enable) {
        enableProperty().set(enable);
    }

    default ColorChooserXYZ getRequest() {
        return requestProperty().get();
    }

    SimpleObjectProperty<ColorChooserXYZ> requestProperty();

    default void setRequest(ColorChooserXYZ request) {
        requestProperty().set(request);
    }
}
