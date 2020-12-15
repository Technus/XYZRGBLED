package com.github.technus.xyzrgbled.javafx.mainWindow;

import com.github.technus.xyzrgbled.javafx.colorChooser.ColorChooserController;
import com.github.technus.xyzrgbled.javafx.colorError.ColorErrorController;
import com.github.technus.xyzrgbled.javafx.colorReading.ColorReadingController;
import com.github.technus.xyzrgbled.javafx.pwmSetting.PwmSettingController;
import com.github.technus.xyzrgbled.model.color.ColorLedXYZ;
import com.github.technus.xyzrgbled.model.hardware.ColorSensor;
import com.github.technus.xyzrgbled.model.hardware.Hardware;
import com.github.technus.xyzrgbled.model.hardware.IHardwareCommunication;
import com.github.technus.xyzrgbled.model.software.Control;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class MainWindowController implements Initializable {
    @FXML private Label temperatureLabel;

    @FXML private HBox chartHBox;
    @FXML private LineChart<Number, Number> ReadingSettingChart = new LineChart<>(new NumberAxis(), new NumberAxis());
    private final XYChart.Series<Number,Number> seriesReadingX=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesReadingY=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesReadingZ=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesSettingX=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesSettingY=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesSettingZ=new XYChart.Series<>();
    @FXML private LineChart<Number, Number> ErrorValuesChart = new LineChart<>(new NumberAxis(), new NumberAxis());
    private final XYChart.Series<Number,Number> seriesErrorX=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesErrorY=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesErrorZ=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesErrorSum =new XYChart.Series<>();
    @FXML private LineChart<Number, Number> PWMSettingsChart = new LineChart<>(new NumberAxis(), new NumberAxis());
    private final XYChart.Series<Number,Number> seriesOutputR=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesOutputG=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesOutputB=new XYChart.Series<>();
    private final XYChart.Series<Number,Number> seriesOutputW=new XYChart.Series<>();

    @FXML private ColorChooserController colorChooserController;
    @FXML private ColorErrorController colorErrorController;
    @FXML private ColorReadingController colorReadingController;

    @FXML private PwmSettingController pwmSettingRController;
    @FXML private PwmSettingController pwmSettingGController;
    @FXML private PwmSettingController pwmSettingBController;
    @FXML private PwmSettingController pwmSettingWController;
    @FXML private PwmSettingController pwmSettingMController;

    @FXML private ChoiceBox<ColorSensor.IntegrationGain> gainChoice;
    @FXML private ChoiceBox<ColorSensor.IntegrationTime> timeChoice;
    @FXML private ChoiceBox<ColorSensor.MeasurementDivider> dividerChoice;
    @FXML private ChoiceBox<ColorSensor.InternalClockFrequency> clockChoice;
    @FXML private ChoiceBox<IHardwareCommunication> serialChoice;
    @FXML private CheckBox measuringCheck;

    @FXML private CheckBox regulatorsEnableChoice;
    @FXML private CheckBox removeOffsetChoice;
    @FXML private CheckBox inverseTransferChoice;
    @FXML private Spinner<Double> regulatorGainSpinner;
    @FXML private Spinner<Double> regulatorIntegrationTimeSpinner;
    @FXML private Spinner<Double> regulatorDifferentiationTimeSpinner;

    private final SimpleIntegerProperty pointCountLimit=new SimpleIntegerProperty(100);

    private final SimpleDoubleProperty gainSetting=new SimpleDoubleProperty(0.01);
    private final ObjectProperty<Double> gainBinding=gainSetting.asObject();

    private final SimpleDoubleProperty integrationSetting=new SimpleDoubleProperty(0.01);
    private final ObjectProperty<Double> integrationBinding=integrationSetting.asObject();

    private final SimpleDoubleProperty differentiationSetting=new SimpleDoubleProperty(0);
    private final ObjectProperty<Double> differentiationBinding=differentiationSetting.asObject();

    private long beginning=System.currentTimeMillis();

    public static Parent start(Hardware hardware, Control control) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindowView.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();
        controller.setHardware(hardware);
        controller.setControl(control);
        return root;
    }

    private void setHardware(Hardware hardware) {
        temperatureLabel.textProperty().bind(new StringBinding() {
            {
                bind(hardware.getColorSensor().readingTemperatureProperty());
            }
            @Override
            protected String computeValue() {
                return "Temperature: "+String.format("%.2f",hardware.getColorSensor().getReadingTemperature())+" degC";
            }
        });
        hardware.getColorSensor().colorProperty().addListener((observable, oldValue, newValue) -> {
            double presentTime=(System.currentTimeMillis()-beginning)/1000D;
            seriesSettingX.getData().add(new XYChart.Data<>(presentTime,colorChooserController.getCurrentColor().getX()));
            seriesSettingY.getData().add(new XYChart.Data<>(presentTime,colorChooserController.getCurrentColor().getY()));
            seriesSettingZ.getData().add(new XYChart.Data<>(presentTime,colorChooserController.getCurrentColor().getZ()));
            seriesReadingX.getData().add(new XYChart.Data<>(presentTime,newValue.getX()));
            seriesReadingY.getData().add(new XYChart.Data<>(presentTime,newValue.getY()));
            seriesReadingZ.getData().add(new XYChart.Data<>(presentTime,newValue.getZ()));

            seriesErrorX.getData().add(new XYChart.Data<>(presentTime,colorErrorController.getErrorX()));
            seriesErrorY.getData().add(new XYChart.Data<>(presentTime,colorErrorController.getErrorY()));
            seriesErrorZ.getData().add(new XYChart.Data<>(presentTime,colorErrorController.getErrorZ()));
            seriesErrorSum.getData().add(new XYChart.Data<>(presentTime,colorErrorController.getErrorSum()));

            seriesOutputR.getData().add(new XYChart.Data<>(presentTime,pwmSettingRController.getPwm().getSetting()));
            seriesOutputG.getData().add(new XYChart.Data<>(presentTime,pwmSettingGController.getPwm().getSetting()));
            seriesOutputB.getData().add(new XYChart.Data<>(presentTime,pwmSettingBController.getPwm().getSetting()));
            seriesOutputW.getData().add(new XYChart.Data<>(presentTime,pwmSettingWController.getPwm().getSetting()));

            if(seriesOutputW.getData().size()>pointCountLimit.get()){
                seriesSettingX.getData().remove(0);
                seriesSettingY.getData().remove(0);
                seriesSettingZ.getData().remove(0);
                seriesReadingX.getData().remove(0);
                seriesReadingY.getData().remove(0);
                seriesReadingZ.getData().remove(0);

                seriesErrorX.getData().remove(0);
                seriesErrorY.getData().remove(0);
                seriesErrorZ.getData().remove(0);
                seriesErrorSum.getData().remove(0);

                seriesOutputR.getData().remove(0);
                seriesOutputG.getData().remove(0);
                seriesOutputB.getData().remove(0);
                seriesOutputW.getData().remove(0);

                XYChart.Data<Number,Number> data=seriesOutputW.getData().get(seriesOutputW.getData().size()-1);

                NumberAxis axis=(NumberAxis)ReadingSettingChart.getXAxis();
                axis.setUpperBound(presentTime);
                axis.setLowerBound(data.getXValue().doubleValue());
                axis=(NumberAxis)ErrorValuesChart.getXAxis();
                axis.setUpperBound(presentTime);
                axis.setLowerBound(data.getXValue().doubleValue());
                axis=(NumberAxis)PWMSettingsChart.getXAxis();
                axis.setUpperBound(presentTime);
                axis.setLowerBound(data.getXValue().doubleValue());
            }
        });
        colorReadingController.readingProperty().bind(hardware.getColorSensor().colorProperty());
        colorReadingController.multiplierProperty().bindBidirectional(hardware.getColorSensor().colorReadingMultiplierProperty());

        pwmSettingRController.setPwm(hardware.getDrivers().stream().filter(driver->driver.getColor()
                .equals(new ColorLedXYZ(Color.RED)  )).findFirst().orElse(null));
        pwmSettingGController.setPwm(hardware.getDrivers().stream().filter(driver->driver.getColor()
                .equals(new ColorLedXYZ(Color.GREEN))).findFirst().orElse(null));
        pwmSettingBController.setPwm(hardware.getDrivers().stream().filter(driver->driver.getColor()
                .equals(new ColorLedXYZ(Color.BLUE) )).findFirst().orElse(null));
        pwmSettingWController.setPwm(hardware.getDrivers().stream().filter(driver->driver.getColor()
                .equals(new ColorLedXYZ(Color.WHITE))).findFirst().orElse(null));
        pwmSettingMController.setPwm(hardware.getDrivers().stream().filter(driver->driver.getColor()
                .equals(new ColorLedXYZ(Color.BLACK))).findFirst().orElse(null));

        serialChoice.setConverter(new StringConverter<IHardwareCommunication>() {
            @Override
            public String toString(IHardwareCommunication object) {
                return object.getInterfaceName();
            }

            @Override
            public IHardwareCommunication fromString(String string) {
                throw new RuntimeException("One shall not make communication thingies here with the name of " + string);
            }
        });
        IHardwareCommunication.suppliers.add(()->serialChoice.getSelectionModel().getSelectedItem()==null?
                emptyList():singletonList(serialChoice.getSelectionModel().getSelectedItem()));
        serialChoice.setOnMouseEntered(event -> IHardwareCommunication.update(serialChoice.getItems()));
        serialChoice.valueProperty().bindBidirectional(hardware.communicationProperty());
        serialChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue!=null){
                oldValue.close();
            }
            if(newValue!=null){
                newValue.open();
            }
        });

        hardware.getColorSensor().measureProperty().addListener((observable, oldValue, newValue) ->
                measuringCheck.setSelected(newValue));
        measuringCheck.selectedProperty().addListener((observable, oldValue, newValue) ->
                hardware.getColorSensor().setMeasureMode(newValue));

        gainChoice.getItems().setAll(ColorSensor.IntegrationGain.values());
        gainChoice.valueProperty().bindBidirectional(hardware.getColorSensor().gainProperty());
        timeChoice.getItems().setAll(ColorSensor.IntegrationTime.values());
        timeChoice.valueProperty().bindBidirectional(hardware.getColorSensor().timeProperty());
        dividerChoice.getItems().setAll(ColorSensor.MeasurementDivider.values());
        dividerChoice.valueProperty().bindBidirectional(hardware.getColorSensor().dividerProperty());
        clockChoice.getItems().setAll(ColorSensor.InternalClockFrequency.values());
        clockChoice.valueProperty().bindBidirectional(hardware.getColorSensor().clockProperty());
    }

    private void setControl(Control control) {
        control.requestProperty().bind(colorChooserController.currentColorProperty());
        control.enableProperty().bindBidirectional(regulatorsEnableChoice.selectedProperty());
        control.enableOffsetRemovalProperty().bindBidirectional(removeOffsetChoice.selectedProperty());
        control.enableTransferFunctionRemovalProperty().bindBidirectional(inverseTransferChoice.selectedProperty());

        regulatorGainSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE,Double.MAX_VALUE,0.01));
        regulatorGainSpinner.getValueFactory().setConverter(new StringConverter<Double>() {
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
        regulatorGainSpinner.getValueFactory().valueProperty().bindBidirectional(gainBinding);

        control.regulatorGainProperty().bindBidirectional(gainSetting);

        regulatorIntegrationTimeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE,Double.MAX_VALUE,.01));
        regulatorIntegrationTimeSpinner.getValueFactory().setConverter(new StringConverter<Double>() {
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
        regulatorIntegrationTimeSpinner.getValueFactory().valueProperty().bindBidirectional(integrationBinding);

        control.regulatorIntegrationTimeProperty().bindBidirectional(integrationSetting);

        regulatorDifferentiationTimeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE,Double.MAX_VALUE,0));
        regulatorDifferentiationTimeSpinner.getValueFactory().setConverter(new StringConverter<Double>() {
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
        regulatorDifferentiationTimeSpinner.getValueFactory().valueProperty().bindBidirectional(differentiationBinding);

        control.regulatorDifferentiationTimeProperty().bindBidirectional(differentiationSetting);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ReadingSettingChart.setTitle("Reading/Setting");
        ReadingSettingChart.setCreateSymbols(false);
        ReadingSettingChart.getData().addAll(seriesSettingX,seriesSettingY,seriesSettingZ,seriesReadingX,seriesReadingY,seriesReadingZ);
        seriesSettingX.setName("rX");
        seriesSettingX.getNode().getStyleClass().add("series-not-r");
        seriesSettingY.setName("rY");
        seriesSettingY.getNode().getStyleClass().add("series-not-g");
        seriesSettingZ.setName("rZ");
        seriesSettingZ.getNode().getStyleClass().add("series-not-b");
        seriesReadingX.setName("yX");
        seriesReadingX.getNode().getStyleClass().add("series-r");
        seriesReadingY.setName("yY");
        seriesReadingY.getNode().getStyleClass().add("series-g");
        seriesReadingZ.setName("yZ");
        seriesReadingZ.getNode().getStyleClass().add("series-b");
        ReadingSettingChart.setStyle("CHART_COLOR_1:#0ff;CHART_COLOR_2:#f0f;CHART_COLOR_3:#ff0;CHART_COLOR_4:#f00;CHART_COLOR_5:#0f0;CHART_COLOR_6:#00f;");
        ReadingSettingChart.applyCss();

        ErrorValuesChart.setTitle("Error Values");
        ErrorValuesChart.setCreateSymbols(false);
        ErrorValuesChart.getData().setAll(seriesErrorX,seriesErrorY,seriesErrorZ, seriesErrorSum);
        seriesErrorX.setName("eX");
        seriesErrorX.getNode().getStyleClass().add("series-r");
        seriesErrorY.setName("eY");
        seriesErrorY.getNode().getStyleClass().add("series-g");
        seriesErrorZ.setName("eZ");
        seriesErrorZ.getNode().getStyleClass().add("series-b");
        seriesErrorSum.setName("eΣ");
        seriesErrorSum.getNode().getStyleClass().add("series-w");
        ErrorValuesChart.setStyle("CHART_COLOR_1:#f00;CHART_COLOR_2:#0f0;CHART_COLOR_3:#00f;CHART_COLOR_4:#fff;");
        ErrorValuesChart.applyCss();

        PWMSettingsChart.setTitle("PWM Settings");
        PWMSettingsChart.setCreateSymbols(false);
        PWMSettingsChart.getData().addAll(seriesOutputR,seriesOutputG,seriesOutputB,seriesOutputW);
        seriesOutputR.setName("uR");
        seriesOutputW.getNode().getStyleClass().add("series-r");
        seriesOutputG.setName("uG");
        seriesOutputW.getNode().getStyleClass().add("series-g");
        seriesOutputB.setName("uB");
        seriesOutputW.getNode().getStyleClass().add("series-b");
        seriesOutputW.setName("uW");
        seriesOutputW.getNode().getStyleClass().add("series-w");
        PWMSettingsChart.setStyle("CHART_COLOR_1:#f00;CHART_COLOR_2:#0f0;CHART_COLOR_3:#00f;CHART_COLOR_4:#fff;");
        PWMSettingsChart.applyCss();

        NumberAxis axis=(NumberAxis)ReadingSettingChart.getXAxis();
        axis.setAnimated(false);
        axis.setForceZeroInRange(false);
        axis=(NumberAxis)ReadingSettingChart.getYAxis();
        axis.setAnimated(false);
        axis.setForceZeroInRange(false);

        axis=(NumberAxis)ErrorValuesChart.getXAxis();
        axis.setAnimated(false);
        axis.setForceZeroInRange(false);
        axis=(NumberAxis)ErrorValuesChart.getYAxis();
        axis.setAnimated(false);

        axis=(NumberAxis)PWMSettingsChart.getXAxis();
        axis.setAnimated(false);
        axis.setForceZeroInRange(false);
        axis=(NumberAxis)PWMSettingsChart.getYAxis();
        axis.setAnimated(false);
        axis.setForceZeroInRange(false);

        chartHBox.getChildren().addAll(ReadingSettingChart, ErrorValuesChart, PWMSettingsChart);

        colorErrorController.errorXProperty().bind(new DoubleBinding() {
            {
                bind(colorChooserController.currentColorProperty());
                bind(colorReadingController.readingProperty());
            }
            @Override
            protected double computeValue() {
                if (colorChooserController.getCurrentColor() != null & colorReadingController.getReading() != null) {
                    return colorReadingController.getReading().getX() - colorChooserController.getCurrentColor().getX();
                }
                return 0;
            }
        });
        colorErrorController.errorYProperty().bind(new DoubleBinding() {
            {
                bind(colorChooserController.currentColorProperty());
                bind(colorReadingController.readingProperty());
            }

            @Override
            protected double computeValue() {
                if (colorChooserController.getCurrentColor() != null & colorReadingController.getReading() != null) {
                    return colorReadingController.getReading().getY() - colorChooserController.getCurrentColor().getY();
                }
                return 0;
            }
        });
        colorErrorController.errorZProperty().bind(new DoubleBinding() {
            {
                bind(colorChooserController.currentColorProperty());
                bind(colorReadingController.readingProperty());
            }

            @Override
            protected double computeValue() {
                if (colorChooserController.getCurrentColor() != null & colorReadingController.getReading() != null) {
                    return colorReadingController.getReading().getZ() - colorChooserController.getCurrentColor().getZ();
                }
                return 0;
            }
        });
    }
}
