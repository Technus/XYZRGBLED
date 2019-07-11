package com.github.technus.xyzrgbled.javafx.pwmSetting;

import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import com.github.technus.xyzrgbled.model.hardware.PulseWidthModulationController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class PwmSettingController implements Initializable {
    @FXML private Slider settingSlider;
    @FXML private Spinner<Integer> settingSpinner;
    @FXML private Circle settingCircle;

    private final SimpleObjectProperty<PulseWidthModulationController> pwm=new SimpleObjectProperty<>();

    private final ChangeListener<ColorLedXYZ>  colorListener= (observable, oldValue, newValue) -> {
        if(newValue!=null){
            settingCircle.setFill(pwm.get().isEnable()?newValue.getColor(): Color.BLACK);
        }
    };

    private final SimpleIntegerProperty setting=new SimpleIntegerProperty();
    private final ObjectProperty<Integer> bindingOfValue=setting.asObject();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(PulseWidthModulationController.MIN_VALUE,PulseWidthModulationController.MAX_VALUE,0));
        settingSpinner.getValueFactory().setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                try{
                    return Integer.parseInt(string);
                }catch (NumberFormatException e){
                    return 0;
                }
            }
        });
        settingSpinner.getValueFactory().valueProperty().bindBidirectional(bindingOfValue);

        settingSlider.setMin(PulseWidthModulationController.MIN_VALUE);
        settingSlider.setMax(PulseWidthModulationController.MAX_VALUE);
        settingSlider.setMajorTickUnit((PulseWidthModulationController.MAX_VALUE-PulseWidthModulationController.MIN_VALUE)/10D);
        settingSlider.setMinorTickCount(10);
        settingSlider.valueProperty().bindBidirectional(setting);

        pwm.addListener((observable, oldValue, newValue) -> {
            if(oldValue!=null){
                oldValue.settingProperty().unbindBidirectional(setting);
                oldValue.colorProperty().removeListener(colorListener);
            }
            if(newValue!=null){
                newValue.settingProperty().bindBidirectional(setting);
                newValue.colorProperty().addListener(colorListener);
                settingCircle.setFill(newValue.getColor().getColor());
            }
        });

        settingCircle.setOnMouseClicked(event -> {
            pwm.get().setEnable(!pwm.get().isEnable());
            settingCircle.setFill(pwm.get().isEnable()?pwm.get().getColor().getColor(): Color.BLACK);
        });
    }


    public PulseWidthModulationController getPwm() {
        return pwm.get();
    }

    public SimpleObjectProperty<PulseWidthModulationController> pwmProperty() {
        return pwm;
    }

    public void setPwm(PulseWidthModulationController pwm) {
        this.pwm.set(pwm);
    }
}
