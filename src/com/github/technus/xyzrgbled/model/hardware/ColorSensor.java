package com.github.technus.xyzrgbled.model.hardware;

import com.github.technus.xyzrgbled.model.color.ColorReadingXYZ;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;

import java.util.HashMap;

public class ColorSensor {
    private static final int SATURATED=32768*3/4,TOOLOW= 32768/3;

    private static final HashMap<Integer,IntegrationGain> mapIntegrationGain=new HashMap<>();
    private static final HashMap<Integer,IntegrationTime> mapIntegrationTime=new HashMap<>();
    private static final HashMap<Integer,InternalClockFrequency> mapInternalClockFrequency=new HashMap<>();
    private static final HashMap<Integer, MeasurementDivider> mapMeasurementDivider=new HashMap<>();

    private final Hardware hardware;

    private final ReadOnlyBooleanWrapper measure=new ReadOnlyBooleanWrapper();
    private volatile boolean gainDownloaded, timeDownloaded, clockDownloaded, dividerDownloaded,reading;
    private final SimpleBooleanProperty writeSettingsOnConnection=new SimpleBooleanProperty();
    private final SimpleObjectProperty<IntegrationGain> gain=new SimpleObjectProperty<>(IntegrationGain.Gain2);
    private final SimpleObjectProperty<IntegrationTime> time=new SimpleObjectProperty<>(IntegrationTime.Time64ms);
    private final SimpleObjectProperty<InternalClockFrequency> clock=new SimpleObjectProperty<>(InternalClockFrequency.Clock1024MHz);
    private final SimpleObjectProperty<MeasurementDivider> divider=new SimpleObjectProperty<>(MeasurementDivider.Divider1);
    private final ReadOnlyDoubleWrapper actualGain=new ReadOnlyDoubleWrapper();

    private final SimpleDoubleProperty colorReadingMultiplier=new SimpleDoubleProperty(100);

    private final ReadOnlyDoubleWrapper readingTemperature=new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper readingX=new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper readingY=new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper readingZ=new ReadOnlyDoubleWrapper();
    private final ReadOnlyBooleanWrapper tooHigh=new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper tooLow=new ReadOnlyBooleanWrapper();

    private final ReadOnlyObjectWrapper<ColorReadingXYZ> color=new ReadOnlyObjectWrapper<>();

    public enum IntegrationGain {
        Gain2048(2048),//err
        Gain1024(1024),//err
        Gain512 (512),//err
        Gain256 (256),
        Gain128 (128),
        Gain64  (64),
        Gain32  (32),
        Gain16  (16),
        Gain8   (8),
        Gain4   (4),
        Gain2   (2),//default
        Gain1   (1);

        public final int gain;

        IntegrationGain(int gain) {
            this.gain = gain;
            add(this);
        }

        private static void add(IntegrationGain me){
            mapIntegrationGain.put(me.gain,me);
        }
    }

    public enum IntegrationTime {
        Time1ms    (1),
        Time2ms    (2),
        Time4ms    (4),
        Time8ms    (8),
        Time16ms   (16),
        Time32ms   (32),
        Time64ms   (64),
        Time128ms  (128),
        Time256ms  (256),
        Time512ms  (512),
        Time1024ms (1024),
        Time2048ms (2048),
        Time4096ms (4096),
        Time8192ms (8192),
        Time16384ms(16384);

        public final int time;

        IntegrationTime(int time) {
            this.time = time;
            add(this);
        }
        private static void add(IntegrationTime me){
            mapIntegrationTime.put(me.time,me);
        }
    }

    public enum InternalClockFrequency {
        Clock1024MHz(1024),
        Clock2048MHz(2048),
        Clock4096MHz(4096),
        Clock8192MHz(8192);

        public final int freq;

        InternalClockFrequency(int freq) {
            this.freq = freq;
            add(this);
        }

        private static void add(InternalClockFrequency me){
            mapInternalClockFrequency.put(me.freq,me);
        }
    }

    public enum MeasurementDivider {
        Divider1  (1),
        Divider2  (2),
        Divider4  (4),
        Divider8  (8),
        Divider16 (16),
        Divider32 (32),
        Divider64 (64),
        Divider128(128),
        Divider256(256);

        public final int div;

        MeasurementDivider(int div) {
            this.div = div;
            add(this);
        }

        private static void add(MeasurementDivider me){
            mapMeasurementDivider.put(me.div,me);
        }
    }

    public ColorSensor(Hardware hardware) {
        this.hardware=hardware;
        hardware.portProperty().openedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setMeasureMode(false);
                if(writeSettingsOnConnection.get()){
                    updateGainCommand();
                    updateTimeCommand();
                    updateDividerCommand();
                    updateClockCommand();
                }else{
                    gainDownloaded = timeDownloaded = dividerDownloaded = clockDownloaded =false;
                    reading=true;
                }
            }
        });

        hardware.portProperty().getEventHandlers().add(s -> Platform.runLater(()->{
            try {
                String[] data = s.split(":");
                if (data[0].equals("T")) {
                    //update color after getting all values
                    color.set(new ColorReadingXYZ(
                            readingX.get()*actualGain.get(),
                            readingY.get()*actualGain.get(),
                            readingZ.get()*actualGain.get(),
                            colorReadingMultiplier.get(),
                            tooHigh.get(),tooLow.get()));

                    measure.set(true);
                    readingTemperature.set(Double.parseDouble(data[1]));
                    return;
                }
                int val = Integer.parseInt(data[1]);
                switch (data[0]) {
                    //todo cat to one event!
                    case "X":
                        measure.set(true);
                        this.readingX.set(val);
                        return;
                    case "Y":
                        measure.set(true);
                        this.readingY.set(val);
                        return;
                    case "Z":
                        measure.set(true);
                        this.readingZ.set(val);
                        return;
                    case "g":
                        measure.set(false);
                        IntegrationGain gain=mapIntegrationGain.get(val);
                        if(gain!=this.gain.get()){
                            if(gainDownloaded){
                                updateGainCommand();
                            }else{
                                this.gain.set(gain);
                                gainDownloaded=true;
                            }
                        }
                        break;
                    case "t":
                        measure.set(false);
                        IntegrationTime time=mapIntegrationTime.get(val);
                        if(time!=this.time.get()){
                            if(timeDownloaded){
                                updateTimeCommand();
                            }else{
                                this.time.set(time);
                                timeDownloaded=true;
                            }
                        }
                        break;
                    case "d":
                        measure.set(false);
                        MeasurementDivider divider=mapMeasurementDivider.get(val);
                        if(divider!=this.divider.get()){
                            if(dividerDownloaded){
                                updateDividerCommand();
                            }else{
                                this.divider.set(divider);
                                dividerDownloaded=true;
                            }
                        }
                        break;
                    case "c":
                        measure.set(false);
                        InternalClockFrequency clock=mapInternalClockFrequency.get(val);
                        if(clock!=this.clock.get()){
                            if(clockDownloaded){
                                updateClockCommand();
                            }else{
                                this.clock.set(clock);
                                clockDownloaded=true;
                            }
                        }
                        break;
                }
                if(reading && gainDownloaded && timeDownloaded && dividerDownloaded && clockDownloaded){
                    reading=false;
                    setMeasureMode(true);
                }
            } catch (Exception ignored) {}
        }));

        gain.addListener((observable, oldValue, newValue) -> {
            if (hardware.portProperty().isOpened() && !measure.get()) {
                setMeasureMode(false);
                updateGainCommand();
                setMeasureMode(true);
            }
        });

        time.addListener((observable, oldValue, newValue) -> {
            if (hardware.portProperty().isOpened() && !measure.get()) {
                setMeasureMode(false);
                updateTimeCommand();
                setMeasureMode(true);
            }
        });

        divider.addListener((observable, oldValue, newValue) -> {
            if (hardware.portProperty().isOpened() && !measure.get()) {
                setMeasureMode(false);
                updateDividerCommand();
                setMeasureMode(true);
            }
        });

        clock.addListener((observable, oldValue, newValue) -> {
            if (hardware.portProperty().isOpened() && !measure.get()) {
                setMeasureMode(false);
                updateClockCommand();
                setMeasureMode(true);
            }
        });

        tooHigh.bind(new BooleanBinding() {
            {
                bind(readingX);
                bind(readingY);
                bind(readingZ);
            }
            @Override
            protected boolean computeValue() {
                return readingX.get()>SATURATED || readingY.get()>SATURATED || readingZ.get()>SATURATED;
            }
        });

        tooLow.bind(new BooleanBinding() {
            {
                bind(readingX);
                bind(readingY);
                bind(readingZ);
                bind(tooHigh);
            }
            @Override
            protected boolean computeValue() {
                return !tooHigh.get() && (readingX.get()<TOOLOW || readingY.get()<TOOLOW || readingZ.get()<TOOLOW);
            }
        });

        actualGain.bind(new DoubleBinding() {
            {
                bind(gain);
                bind(time);
                bind(divider);
            }
            @Override
            protected double computeValue() {
                if(gain.get()!=null && time.get()!=null && divider.get()!=null){
                    return ((double) divider.get().div)/gain.get().gain/time.get().time;
                }
                return 0;
            }
        });
    }

    public void setMeasureMode(boolean measure) {
        if (measure) {
            hardware.portProperty().queueDataToSend(new byte[]{(byte) 0b11110101}, 100);
        } else {
            hardware.portProperty().queueDataToSend(new byte[]{(byte) 0b11110100}, 1000);
        }
        this.measure.set(measure);
    }

    private void updateGainCommand() {
        hardware.portProperty().queueDataToSend(new byte[]{(byte)(0b11000000|gain.get().ordinal())});
    }

    private void updateTimeCommand() {
        hardware.portProperty().queueDataToSend(new byte[]{(byte) (0b11010000 | time.get().ordinal())});
    }

    private void updateClockCommand() {
        hardware.portProperty().queueDataToSend(new byte[]{(byte) (0b11110000 | clock.get().ordinal())});
    }

    private void updateDividerCommand() {
        hardware.portProperty().queueDataToSend(new byte[]{(byte) (0b11100000 | divider.get().ordinal())});
    }

    public IntegrationGain getGain() {
        return gain.get();
    }

    public SimpleObjectProperty<IntegrationGain> gainProperty() {
        return gain;
    }

    public void setGain(IntegrationGain gain) {
        this.gain.set(gain);
    }

    public IntegrationTime getTime() {
        return time.get();
    }

    public SimpleObjectProperty<IntegrationTime> timeProperty() {
        return time;
    }

    public void setTime(IntegrationTime time) {
        this.time.set(time);
    }

    public InternalClockFrequency getClock() {
        return clock.get();
    }

    public SimpleObjectProperty<InternalClockFrequency> clockProperty() {
        return clock;
    }

    public void setClock(InternalClockFrequency clock) {
        this.clock.set(clock);
    }

    public MeasurementDivider getDivider() {
        return divider.get();
    }

    public SimpleObjectProperty<MeasurementDivider> dividerProperty() {
        return divider;
    }

    public void setDivider(MeasurementDivider divider) {
        this.divider.set(divider);
    }

    public double getActualGain() {
        return actualGain.get();
    }

    public ReadOnlyDoubleProperty actualGainProperty() {
        return actualGain.getReadOnlyProperty();
    }

    public double getReadingX() {
        return readingX.get();
    }

    public ReadOnlyDoubleProperty readingXProperty() {
        return readingX.getReadOnlyProperty();
    }

    public double getReadingY() {
        return readingY.get();
    }

    public ReadOnlyDoubleProperty readingYProperty() {
        return readingY.getReadOnlyProperty();
    }

    public double getReadingZ() {
        return readingZ.get();
    }

    public ReadOnlyDoubleProperty readingZProperty() {
        return readingZ.getReadOnlyProperty();
    }

    public double getReadingTemperature() {
        return readingTemperature.get();
    }

    public ReadOnlyDoubleProperty readingTemperatureProperty() {
        return readingTemperature.getReadOnlyProperty();
    }

    public ColorReadingXYZ getColor() {
        return color.get();
    }

    public ReadOnlyObjectProperty<ColorReadingXYZ> colorProperty() {
        return color.getReadOnlyProperty();
    }

    public boolean isMeasure() {
        return measure.get();
    }

    public ReadOnlyBooleanProperty measureProperty() {
        return measure.getReadOnlyProperty();
    }

    public boolean isTooHigh() {
        return tooHigh.get();
    }

    public ReadOnlyBooleanProperty tooHighProperty() {
        return tooHigh.getReadOnlyProperty();
    }

    public boolean isTooLow() {
        return tooLow.get();
    }

    public ReadOnlyBooleanProperty tooLowProperty() {
        return tooLow.getReadOnlyProperty();
    }

    public double getColorReadingMultiplier() {
        return colorReadingMultiplier.get();
    }

    public SimpleDoubleProperty colorReadingMultiplierProperty() {
        return colorReadingMultiplier;
    }

    public void setColorReadingMultiplier(double colorReadingMultiplier) {
        this.colorReadingMultiplier.set(colorReadingMultiplier);
    }
}
