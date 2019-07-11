package com.github.technus.xyzrgbled.javafx.colorReading;

import com.github.technus.xyzrgbled.model.color.ColorReadingXYZ;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorReadingController implements Initializable {
    @FXML private CheckBox lowCheckbox;
    @FXML private CheckBox highCheckbox;
    @FXML private ColorPicker colorPicker;
    @FXML private Spinner<Double> multiplierSpinner;
    @FXML private TextField ReadingTextFieldX;
    @FXML private TextField ReadingTextFieldY;
    @FXML private TextField ReadingTextFieldZ;
    @FXML private Rectangle colorBox;

    private final SimpleObjectProperty<ColorReadingXYZ> reading=new SimpleObjectProperty<>(new ColorReadingXYZ());

    private final ChangeListener<Color> listener= (observable, oldValue, newValue) -> {
        if(newValue!=null){
            colorBox.setFill(newValue);
            colorPicker.setValue(newValue);
        }
    };

    private final SimpleDoubleProperty multiplier =new SimpleDoubleProperty();
    private final ObjectProperty<Double> bindingOfValue= multiplier.asObject();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        multiplierSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,100));
        multiplierSpinner.getValueFactory().setConverter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                if(object.longValue()==object){
                    return ""+object.longValue();
                }else {
                    return object.toString();
                }
            }

            @Override
            public Double fromString(String string) {
                try{
                    return Double.parseDouble(string);
                }catch (NumberFormatException e){
                    return 1D;
                }
            }
        });
        multiplierSpinner.getValueFactory().valueProperty().bindBidirectional(bindingOfValue);

        reading.addListener((observable, oldValue, newValue) -> {
            if(oldValue!=null) {
                oldValue.colorProperty().removeListener(listener);
            }
            if(newValue!=null){
                newValue.colorProperty().addListener(listener);
                colorBox.setFill(newValue.getColor());
                colorPicker.setValue(newValue.getColor());
                ReadingTextFieldX.setText(""+newValue.getX());
                ReadingTextFieldY.setText(""+newValue.getY());
                ReadingTextFieldZ.setText(""+newValue.getZ());
                lowCheckbox.setSelected(newValue.isTooLow());
                highCheckbox.setSelected(newValue.isTooHigh());
            }
        });
    }

    public ColorReadingXYZ getReading() {
        return reading.get();
    }

    public SimpleObjectProperty<ColorReadingXYZ> readingProperty() {
        return reading;
    }

    public void setReading(ColorReadingXYZ reading) {
        this.reading.set(reading);
    }

    public double getMultiplier() {
        return multiplier.get();
    }

    public SimpleDoubleProperty multiplierProperty() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier.set(multiplier);
    }
}
