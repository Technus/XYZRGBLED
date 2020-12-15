package com.github.technus.xyzrgbled.model.software;

import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import com.github.technus.xyzrgbled.model.color.ColorReadingXYZ;
import com.github.technus.xyzrgbled.model.hardware.PulseWidthModulationController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Neural implements IRegulator  {
    @Override
    public SimpleObjectProperty<PulseWidthModulationController> pwmProperty() {
        return null;
    }

    @Override
    public SimpleDoubleProperty maxProperty() {
        return null;
    }

    @Override
    public SimpleDoubleProperty minProperty() {
        return null;
    }

    @Override
    public SimpleDoubleProperty settingProperty() {
        return null;
    }

    @Override
    public double getNextSetting(double reading, double requestedValue) {
        return 0;
    }

    @Override
    public SimpleObjectProperty<ColorReadingXYZ> readingProperty() {
        return null;
    }

    @Override
    public SimpleBooleanProperty enableProperty() {
        return null;
    }

    @Override
    public SimpleObjectProperty<ColorChooserXYZ> requestProperty() {
        return null;
    }
}
