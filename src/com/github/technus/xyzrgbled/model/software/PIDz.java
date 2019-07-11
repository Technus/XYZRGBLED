package com.github.technus.xyzrgbled.model.software;

import com.github.technus.xyzrgbled.model.color.ColorChooserXYZ;
import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import com.github.technus.xyzrgbled.model.color.ColorReadingXYZ;
import com.github.technus.xyzrgbled.model.hardware.PulseWidthModulationController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Random;

/**
 * Created by danie_000 on 01.11.2017.
 */
public class PIDz {
    private final SimpleDoubleProperty regulatorGain=new SimpleDoubleProperty(.0001);
    private final SimpleDoubleProperty integrationTime=new SimpleDoubleProperty(.1);
    private final SimpleDoubleProperty differentiationTime=new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty discreteTimeStep=new SimpleDoubleProperty(1);
    private final SimpleDoubleProperty max =new SimpleDoubleProperty(Double.MAX_VALUE);
    private final SimpleDoubleProperty min =new SimpleDoubleProperty(-Double.MAX_VALUE);
    private final SimpleDoubleProperty transferCoupling=new SimpleDoubleProperty(1);
    private final SimpleDoubleProperty inputOffset =new SimpleDoubleProperty(0);
    private final SimpleBooleanProperty enable=new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty enableOffset=new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty enableInverseTransferCoupling=new SimpleBooleanProperty(false);
    private double integratorState, previousError, previousSetting;

    private final SimpleObjectProperty<PulseWidthModulationController> pwm=new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ColorReadingXYZ> reading=new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ColorChooserXYZ> request=new SimpleObjectProperty<>();
    private final SimpleDoubleProperty setting=new SimpleDoubleProperty();
    private final SimpleIntegerProperty settingInt=new SimpleIntegerProperty();

    private final Control control;

    private final Random random=new Random();

    public PIDz(PulseWidthModulationController pwm,Control control) {
        this.control=control;
        this.pwm.set(pwm);
        pwm.settingProperty().bindBidirectional(settingInt);

        reading.addListener((observable, oldValue, newValue) -> {
            if(pwm.isEnable()) {
                if (enable.get() && newValue != null) {
                    if (mustDecrease()) {//todo make faster and better
                        setting.set(setting.get() - 1);
                    } else if (mustIncrease()) {
                        setting.set(setting.get() + 1);
                    } else {
                        setting.set(getNextSetting(getReadingValue(newValue), getRequestedValue(request.get())));
                    }
                }
                int baseSetting = (int) setting.get();
                settingInt.set(baseSetting + (random.nextDouble() < setting.get() - baseSetting ? 1 : 0));
            }else{
                resetIntegrator();
            }
        });
    }

    private double getReadingValue(ColorReadingXYZ reading){
        if(reading==null){
            return 0;
        }
        ColorLedXYZ led=pwm.get().getColor();
        return reading.getX()*led.getX()+reading.getY()*led.getY()+reading.getZ()*led.getZ();
    }

    private double getRequestedValue(ColorChooserXYZ request){
        if(request==null){
            return 0;
        }
        ColorLedXYZ led=pwm.get().getColor();
        return request.getX()*led.getX()+request.getY()*led.getY()+request.getZ()*led.getZ();
    }

    private boolean mustIncrease(){
        if(pwm.get().getBroadBand() && setting.get()<max.get()-1){
            for (PIDz regulator : control.getRegulators()) {
                if(!regulator.pwm.get().getBroadBand() && regulator.setting.get()==regulator.min.get()){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    private boolean mustDecrease(){
        if(pwm.get().getBroadBand() && setting.get()>min.get()+1){
            for (PIDz regulator : control.getRegulators()) {
                if(!regulator.pwm.get().getBroadBand() && regulator.setting.get()==regulator.min.get()){
                    return true;
                }
            }
        }
        return false;
    }

    public double getNextSetting(double reading, double requestedValue) {
        if(previousSetting ==0){
            inputOffset.set((inputOffset.get()+reading)/2D);
        }else {
            transferCoupling.set((transferCoupling.get()+reading/previousSetting)/2D);
        }
        if(enableOffset.get()){
            reading-= inputOffset.get();
        }

        double decoupledGain=enableInverseTransferCoupling.get()?regulatorGain.get() / transferCoupling.get():regulatorGain.get();
        double error=requestedValue-reading;

        if (integrationTime.get() != 0) integratorState += (1D / integrationTime.get()) * error * discreteTimeStep.get();
        double setting = decoupledGain * (error + integratorState + differentiationTime.get() * (error - previousError) / discreteTimeStep.get());
        previousError = error;
        if (setting > max.get()) setting = max.get();
        else if (setting < min.get()) setting = min.get();
        if (integrationTime.get() != 0) {
            if (integratorState > max.get()/decoupledGain) integratorState = max.get()/decoupledGain;
            else if (integratorState < min.get()/decoupledGain) integratorState = min.get()/decoupledGain;
        }
        //System.out.println(error+" "+ setting);
        return previousSetting =setting;
    }

    private void resetIntegrator() {
        integratorState =previousError=previousSetting=0;
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

    public double getIntegrationTime() {
        return integrationTime.get();
    }

    public SimpleDoubleProperty integrationTimeProperty() {
        return integrationTime;
    }

    public void setIntegrationTime(double integrationTime) {
        this.integrationTime.set(integrationTime);
    }

    public double getDifferentiationTime() {
        return differentiationTime.get();
    }

    public SimpleDoubleProperty differentiationTimeProperty() {
        return differentiationTime;
    }

    public void setDifferentiationTime(double differentiationTime) {
        this.differentiationTime.set(differentiationTime);
    }

    public double getDiscreteTimeStep() {
        return discreteTimeStep.get();
    }

    public SimpleDoubleProperty discreteTimeStepProperty() {
        return discreteTimeStep;
    }

    public void setDiscreteTimeStep(double discreteTimeStep) {
        this.discreteTimeStep.set(discreteTimeStep);
    }

    public double getMax() {
        return max.get();
    }

    public SimpleDoubleProperty maxProperty() {
        return max;
    }

    public void setMax(double max) {
        this.max.set(max);
    }

    public double getMin() {
        return min.get();
    }

    public SimpleDoubleProperty minProperty() {
        return min;
    }

    public void setMin(double min) {
        this.min.set(min);
    }

    public double getTransferCoupling() {
        return transferCoupling.get();
    }

    public SimpleDoubleProperty transferCouplingProperty() {
        return transferCoupling;
    }

    public void setTransferCoupling(double transferCoupling) {
        this.transferCoupling.set(transferCoupling);
    }

    public double getInputOffset() {
        return inputOffset.get();
    }

    public SimpleDoubleProperty inputOffsetProperty() {
        return inputOffset;
    }

    public void setInputOffset(double inputOffset) {
        this.inputOffset.set(inputOffset);
    }

    public boolean isEnableOffset() {
        return enableOffset.get();
    }

    public SimpleBooleanProperty enableOffsetProperty() {
        return enableOffset;
    }

    public void setEnableOffset(boolean enableOffset) {
        this.enableOffset.set(enableOffset);
    }

    public boolean isEnableInverseTransferCoupling() {
        return enableInverseTransferCoupling.get();
    }

    public SimpleBooleanProperty enableInverseTransferCouplingProperty() {
        return enableInverseTransferCoupling;
    }

    public void setEnableInverseTransferCoupling(boolean enableInverseTransferCoupling) {
        this.enableInverseTransferCoupling.set(enableInverseTransferCoupling);
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

    public ColorReadingXYZ getReading() {
        return reading.get();
    }

    public SimpleObjectProperty<ColorReadingXYZ> readingProperty() {
        return reading;
    }

    public void setReading(ColorReadingXYZ reading) {
        this.reading.set(reading);
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

    public ColorChooserXYZ getRequest() {
        return request.get();
    }

    public SimpleObjectProperty<ColorChooserXYZ> requestProperty() {
        return request;
    }

    public void setRequest(ColorChooserXYZ request) {
        this.request.set(request);
    }
}
