package com.github.technus.xyzrgbled.model.hardware;

import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

public class PulseWidthModulationController {
    public static final int MAX_VALUE = 4095, MIN_VALUE = 0;
    private static final int MAX_CHANNEL = 15, MIN_CHANNEL = 0;

    private final Hardware hardware;
    private final ReadOnlyBooleanWrapper broadBand =new ReadOnlyBooleanWrapper();
    private final SimpleBooleanProperty enable=new SimpleBooleanProperty(true);
    private final SimpleIntegerProperty setting = new SimpleIntegerProperty();
    private final SimpleIntegerProperty channel = new SimpleIntegerProperty();
    private final SimpleObjectProperty<ColorLedXYZ> color=new SimpleObjectProperty<>();

    public PulseWidthModulationController(Hardware hardware, int channel, ColorLedXYZ color) {
        this.hardware = hardware;
        ChangeListener<Boolean> openListener = (observable1, oldValue1, newValue1) -> updateSetting();
        hardware.communicationProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue!=null){
                oldValue.openedProperty().removeListener(openListener);
            }
            if(newValue!=null){
                if(newValue.isOpened()){
                    updateSetting();
                }
                newValue.openedProperty().addListener(openListener);
            }
        });

        setting.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > MAX_VALUE) {
                setting.set(MAX_VALUE);
            } else if (newValue.intValue() < MIN_VALUE) {
                setting.set(MIN_VALUE);
            } else {
                updateSetting();
            }
        });

        this.channel.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue()>MAX_CHANNEL){
                this.channel.set(MAX_CHANNEL);
            }else if (newValue.intValue()<MIN_CHANNEL){
                this.channel.set(MIN_CHANNEL);
            }else {
                updateSetting();
            }
        });

        this.enable.addListener((observable, oldValue, newValue) -> updateSetting());

        this.channel.set(channel);
        this.color.set(color);
        this.broadBand.bind(new BooleanBinding() {
            {
                bind(PulseWidthModulationController.this.color);
            }
            @Override
            protected boolean computeValue() {
                return PulseWidthModulationController.this.color.get().getColor().getSaturation()<0.5;
            }
        });
    }

    private void updateSetting() {
        if(enable.get()) {
            if (hardware.getCommunication().isOpened()) {
                hardware.getCommunication().queueDataToSend(new byte[]{
                        (byte) (0x40 | (setting.get() & 0x3F)),
                        (byte) (0x80 | ((setting.get() >>> 6) & 0x3F)),
                        (byte) channel.get()
                });
            }
        }else{
            setting.set(0);
        }
    }

    public int getSetting() {
        return setting.get();
    }

    public SimpleIntegerProperty settingProperty() {
        return setting;
    }

    public void setSetting(int setting) {
        this.setting.set(setting);
    }

    public int getChannel() {
        return channel.get();
    }

    public SimpleIntegerProperty channelProperty() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel.set(channel);
    }

    public ColorLedXYZ getColor() {
        return color.get();
    }

    public SimpleObjectProperty<ColorLedXYZ> colorProperty() {
        return color;
    }

    public void setColor(ColorLedXYZ color) {
        this.color.set(color);
    }

    public boolean getBroadBand() {
        return broadBand.get();
    }

    public ReadOnlyBooleanProperty broadBandProperty() {
        return broadBand.getReadOnlyProperty();
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
}
