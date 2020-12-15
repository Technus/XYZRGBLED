package com.github.technus.xyzrgbled.model.hardware;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SerialPortProperty extends SimpleObjectProperty<SerialPort> implements IHardwareCommunication{
    private static List<SerialPort> portsList=new ArrayList<>();
    public static List<SerialPortProperty> updatePorts(){
        List<String> namesNew= Arrays.asList(SerialPortList.getPortNames());
        List<String> namesOld=new ArrayList<>();
        List<SerialPort> removals=new ArrayList<>();
        portsList.forEach(serialPort -> {
            if(!namesNew.contains(serialPort.getPortName())){
                try{
                    serialPort.removeEventListener();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                try {
                    serialPort.closePort();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                removals.add(serialPort);
            }else{
                namesOld.add(serialPort.getPortName());
            }
        });
        portsList.removeAll(removals);
        namesNew.forEach(name->{
            if(!namesOld.contains(name)){
                portsList.add(new SerialPort(name));
            }
        });
        return portsList.stream().map(serialPort-> {
            SerialPortProperty serialPortProperty = new SerialPortProperty();
            serialPortProperty.set(serialPort);
            return serialPortProperty;
        }).collect(Collectors.toList());
    }

    private final ReadOnlyStringWrapper nameBuffer=new ReadOnlyStringWrapper("unconfigured");
    private final ReadOnlyStringWrapper communicationName=new ReadOnlyStringWrapper();
    {
        communicationName.bind(new StringBinding() {
            {
                bind(nameBuffer,SerialPortProperty.this);
            }
            @Override
            protected String computeValue() {
                return SerialPortProperty.this.get() == null ? nameBuffer.get() + " missing" : nameBuffer.get();
            }
        });
    }
    private Thread serialWriter;

    private final ReadOnlyBooleanWrapper opened =new ReadOnlyBooleanWrapper();
    private final ObservableList<Consumer<String>> eventHandlers= FXCollections.observableArrayList();
    private final Queue<QueuedRx> dataToSend=new ConcurrentLinkedQueue<>();
    private final StringBuffer dataReceived = new StringBuffer();

    public SerialPortProperty(){
        addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                nameBuffer.set(newValue.getPortName());
            }
        });
    }

    @Override
    public void open() {
        SerialPort port=get();
        if (port != null) {
            if (port.isOpened()) {
                set(null);
            } else {
                try {
                    if (port.openPort()) {
                        port.addEventListener(serialPortEvent -> {
                            if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0) {
                                try {
                                    String newStr = port.readString();
                                    for (char c : newStr.toCharArray()) {
                                        switch (c) {
                                            case '\r':
                                                String cmd = dataReceived.toString();
                                                dataReceived.delete(0, cmd.length());
                                                eventHandlers.forEach(serialPortEventConsumer ->
                                                        serialPortEventConsumer.accept(cmd));
                                                break;
                                            default:
                                                dataReceived.append(c);
                                            case '\n':
                                        }
                                    }
                                    //System.out.print(newStr);
                                } catch (Exception E) {
                                    E.printStackTrace();
                                }
                            }
                        });
                        serialWriter=new Thread(()-> {
                            while (true) {
                                SerialPort serialPort=this.get();
                                if (serialPort != null) {
                                    if (serialPort.isOpened() && opened.get()) {
                                        try {
                                            while(!dataToSend.isEmpty()) {
                                                QueuedRx queuedRx=dataToSend.poll();
                                                if(queuedRx.data!=null) {
                                                    serialPort.writeBytes(queuedRx.data);
                                                }
                                                if(queuedRx.delayMs>0) {
                                                    Thread.sleep(queuedRx.delayMs);
                                                }
                                            }
                                        } catch (SerialPortException e) {
                                            e.printStackTrace();
                                            Platform.runLater(() -> this.set(null));
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException ignored) {
                                                return;
                                            }
                                        } catch (InterruptedException ignored) {}
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            return;
                                        }
                                    } else {
                                        Platform.runLater(() -> this.set(null));
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            return;
                                        }
                                    }
                                }else{
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        return;
                                    }
                                }
                            }
                        },"SerialWriter");
                        serialWriter.start();
                        opened.set(true);
                    }
                } catch (SerialPortException e) {
                    try {
                        port.removeEventListener();
                    } catch (SerialPortException ignored) {
                    }
                    set(null);
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            serialWriter.interrupt();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            get().closePort();
        } catch (NullPointerException | SerialPortException e) {
            e.printStackTrace();
        }
        set(null);
    }

    public ReadOnlyBooleanProperty openedProperty() {
        return opened.getReadOnlyProperty();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public boolean queueDataToSend(byte[] bytes,int delayAfter){
        return dataToSend.offer(new QueuedRx(bytes,delayAfter));
    }

    public ObservableList<Consumer<String>> getEventHandlers() {
        return eventHandlers;
    }

    @Override
    public ReadOnlyStringProperty interfaceNameProperty() {
        return communicationName.getReadOnlyProperty();
    }
}
