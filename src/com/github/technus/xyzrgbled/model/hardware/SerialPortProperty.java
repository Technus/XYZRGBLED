package com.github.technus.xyzrgbled.model.hardware;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class SerialPortProperty extends SimpleObjectProperty<SerialPort> {
    public static void updatePorts(ObservableList<SerialPort> portsList){
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
    }

    private final ReadOnlyBooleanWrapper opened =new ReadOnlyBooleanWrapper();
    private final Thread serialWriter;

    private final ObservableList<Consumer<String>> eventHandlers= FXCollections.observableArrayList();
    private final Queue<QueuedRx> dataToSend=new ConcurrentLinkedQueue<>();
    private final StringBuffer dataReceived = new StringBuffer();

    public SerialPortProperty(){
        addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && oldValue.isOpened()) {
                try{
                    oldValue.removeEventListener();
                } catch (SerialPortException ignored) {}
                try {
                    if(oldValue.closePort()){
                        opened.set(false);
                        dataToSend.clear();
                    }
                } catch (SerialPortException e) {
                    throw new RuntimeException("Unable to dispose previous port", e);
                }
            }
            if (newValue != null) {
                if (newValue.isOpened()) {
                    SerialPortProperty.this.set(null);
                } else {
                    try {
                        if(newValue.openPort()){
                            newValue.addEventListener(serialPortEvent -> {
                                if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue()>0) {
                                    try {
                                        String newStr=SerialPortProperty.this.get().readString();
                                        for(char c:newStr.toCharArray()){
                                            switch (c){
                                                case '\r':
                                                    String cmd= dataReceived.toString();
                                                    dataReceived.delete(0,cmd.length());
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
                            opened.set(true);
                        }
                    } catch (SerialPortException e) {
                        try {
                            newValue.removeEventListener();
                        } catch (SerialPortException ignored) {}
                        SerialPortProperty.this.set(null);
                    }
                }
            }
        });

        serialWriter =new Thread(()-> {
            while (true) {
                SerialPort port=this.get();
                if (port != null) {
                    if (port.isOpened() && opened.get()) {
                        try {
                            while(!dataToSend.isEmpty()) {
                                QueuedRx queuedRx=dataToSend.poll();
                                if(queuedRx.data!=null) {
                                    port.writeBytes(queuedRx.data);
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
    }

    public boolean isOpened() {
        return opened.get();
    }

    public ReadOnlyBooleanProperty openedProperty() {
        return opened.getReadOnlyProperty();
    }

    @Override
    protected void finalize() throws Throwable {
        serialWriter.interrupt();
        while(serialWriter.isAlive()){
            Thread.sleep(10);
        }
        set(null);
        super.finalize();
    }

    public boolean queueDataToSend(byte[] bytes){
        return queueDataToSend(bytes,0);
    }

    public boolean queueDataToSend(byte[] bytes,int delayAfter){
        return dataToSend.offer(new QueuedRx(bytes,delayAfter));
    }

    private static class QueuedRx{
        private final byte[] data;
        private final int delayMs;

        private QueuedRx(byte[] data, int delayMs) {
            this.data = data;
            this.delayMs = delayMs;
        }
    }

    public ObservableList<Consumer<String>> getEventHandlers() {
        return eventHandlers;
    }
}
