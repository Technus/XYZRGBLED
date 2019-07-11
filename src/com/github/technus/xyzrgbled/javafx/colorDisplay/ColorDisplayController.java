package com.github.technus.xyzrgbled.javafx.colorDisplay;

import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorDisplayController implements Initializable {
    @FXML private TextField ReadingTextFieldX;
    @FXML private TextField ReadingTextFieldY;
    @FXML private TextField ReadingTextFieldZ;
    @FXML private Rectangle colorBox;

    private final SimpleObjectProperty<ColorChooserXYZ> selection=new SimpleObjectProperty<>(new ColorChooserXYZ());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selection.addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                colorBox.setFill(newValue.getColor());
                ReadingTextFieldX.setText(""+newValue.getX());
                ReadingTextFieldY.setText(""+newValue.getY());
                ReadingTextFieldZ.setText(""+newValue.getZ());
            }
        });
    }

    public ColorChooserXYZ getSelection() {
        return selection.get();
    }

    public SimpleObjectProperty<ColorChooserXYZ> selectionProperty() {
        return selection;
    }

    public void setSelection(ColorChooserXYZ reading) {
        this.selection.set(reading);
    }
}
