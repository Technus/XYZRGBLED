package com.github.technus.xyzrgbled.javafx.colorChooser;

import com.github.technus.xyzrgbled.javafx.colorDisplay.ColorDisplayController;
import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorChooserController implements Initializable {
    @FXML private ColorPicker picker;
    @FXML private Spinner<Double> mult;
    @FXML private ColorDisplayController colorDisplayController;

    private ReadOnlyObjectWrapper<ColorChooserXYZ> currentColor=new ReadOnlyObjectWrapper<>(new ColorChooserXYZ());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mult.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,1));
        mult.getValueFactory().setConverter(new StringConverter<Double>() {
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
        mult.valueProperty().addListener((observable, oldValue, newValue) -> updateCurrentColorProperty());
        picker.setValue(Color.WHITE);
        picker.valueProperty().addListener((observable, oldValue, newValue) -> updateCurrentColorProperty());
        updateCurrentColorProperty();
    }

    private void updateCurrentColorProperty(){
        ColorChooserXYZ colorChooserXYZ=new ColorChooserXYZ(picker.getValue(),mult.getValue());
        currentColor.setValue(colorChooserXYZ);
        colorDisplayController.setSelection(colorChooserXYZ);
    }

    public ColorChooserXYZ getCurrentColor() {
        return currentColor.get();
    }

    public ReadOnlyObjectProperty<ColorChooserXYZ> currentColorProperty() {
        return currentColor.getReadOnlyProperty();
    }
}
