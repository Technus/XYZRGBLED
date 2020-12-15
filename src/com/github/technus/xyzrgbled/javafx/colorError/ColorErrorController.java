package com.github.technus.xyzrgbled.javafx.colorError;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorErrorController implements Initializable {
    private final ReadOnlyDoubleWrapper ErrorSum=new ReadOnlyDoubleWrapper();
    private final SimpleDoubleProperty ErrorX=new SimpleDoubleProperty();
    private final SimpleDoubleProperty ErrorY=new SimpleDoubleProperty();
    private final SimpleDoubleProperty ErrorZ=new SimpleDoubleProperty();

    @FXML private TextField ReadingTextField;
    @FXML private TextField ReadingTextFieldX;
    @FXML private TextField ReadingTextFieldY;
    @FXML private TextField ReadingTextFieldZ;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ReadingTextFieldX.textProperty().bind(new StringBinding() {
            {
                bind(ErrorX);
            }
            @Override
            protected String computeValue() {
                if(ErrorX.get()<0){
                    return ""+ErrorX.get();
                }
                return "+"+ErrorX.get();
            }
        });
        ReadingTextFieldY.textProperty().bind(new StringBinding() {
            {
                bind(ErrorY);
            }
            @Override
            protected String computeValue() {
                if(ErrorY.get()<0){
                    return ""+ErrorY.get();
                }
                return "+"+ErrorY.get();
            }
        });
        ReadingTextFieldZ.textProperty().bind(new StringBinding() {
            {
                bind(ErrorZ);
            }
            @Override
            protected String computeValue() {
                if(ErrorZ.get()<0){
                    return ""+ErrorZ.get();
                }
                return "+"+ErrorZ.get();
            }
        });
        ErrorSum.bind(new DoubleBinding() {
            {
                bind(ErrorX,ErrorY,ErrorZ);
            }
            @Override
            protected double computeValue() {
                return (Math.abs(ErrorX.get())+Math.abs(ErrorY.get())+Math.abs(ErrorZ.get()))/3D;
            }
        });
        ReadingTextField.textProperty().bind(ErrorSum.asString());
    }

    public double getErrorX() {
        return ErrorX.get();
    }

    public SimpleDoubleProperty errorXProperty() {
        return ErrorX;
    }

    public void setErrorX(double errorX) {
        this.ErrorX.set(errorX);
    }

    public double getErrorY() {
        return ErrorY.get();
    }

    public SimpleDoubleProperty errorYProperty() {
        return ErrorY;
    }

    public void setErrorY(double errorY) {
        this.ErrorY.set(errorY);
    }

    public double getErrorZ() {
        return ErrorZ.get();
    }

    public SimpleDoubleProperty errorZProperty() {
        return ErrorZ;
    }

    public void setErrorZ(double errorZ) {
        this.ErrorZ.set(errorZ);
    }

    public double getErrorSum() {
        return ErrorSum.get();
    }

    public ReadOnlyDoubleProperty errorSumProperty() {
        return ErrorSum.getReadOnlyProperty();
    }
}
