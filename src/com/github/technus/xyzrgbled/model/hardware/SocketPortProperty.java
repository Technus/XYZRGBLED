package com.github.technus.xyzrgbled.model.hardware;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class SocketPortProperty extends SimpleObjectProperty<Socket> implements IHardwareCommunication {
    public static List<SocketPortProperty> generateConnection(){
        return Collections.singletonList(new SocketPortProperty());
    }

    private final ReadOnlyStringWrapper name=new ReadOnlyStringWrapper("Socket");
    private final ReadOnlyBooleanWrapper opened =new ReadOnlyBooleanWrapper();
    private final ObservableList<Consumer<String>> eventHandlers= FXCollections.observableArrayList();
    private final Queue<QueuedRx> dataToSend=new ConcurrentLinkedQueue<>();
    private Thread writerRunner,reader;

    @Override
    public void close() {
        try {
            writerRunner.interrupt();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            reader.interrupt();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            get().close();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        set(null);
    }

    @Override
    public void open() {
        try {
            TextInputDialog textInputDialog = new TextInputDialog("192.168.0.80");
            textInputDialog.setContentText("IPv4:");
            textInputDialog.setHeaderText("Color sensor IP:");
            textInputDialog.setTitle("Color sensor IP selector");
            set(new Socket(textInputDialog.showAndWait().orElse("127.0.0.1"),9001));
            writerRunner = new Thread(() -> {
                while (get().isBound() || !Thread.currentThread().isInterrupted()) {
                    name.set(get().getInetAddress().getHostAddress());
                    InputStreamReader inputStreamReader = null;
                    OutputStream outputStreamWriter = null;
                    try {
                        inputStreamReader = new InputStreamReader(get().getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStreamWriter = get().getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (outputStreamWriter != null && inputStreamReader != null) {
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        reader = new Thread(() -> {
                            while (get().isConnected() || !Thread.currentThread().isInterrupted()) {
                                try {
                                    String s = bufferedReader.readLine();
                                    eventHandlers.forEach(stringConsumer -> stringConsumer.accept(s));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        },"SocketReader");
                        reader.start();
                        while (get().isConnected() || !Thread.currentThread().isInterrupted()) {
                            while(!dataToSend.isEmpty()) {
                                QueuedRx queuedRx = dataToSend.poll();
                                if (queuedRx.data != null) {
                                    try {
                                        outputStreamWriter.write(queuedRx.data);
                                        outputStreamWriter.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (queuedRx.delayMs > 0) {
                                    try {
                                        Thread.sleep(queuedRx.delayMs);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        name.set("Socket :(");
                    }
                    try {
                        get().close();
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                }
                close();
            },"SocketWriterRunner");
            writerRunner.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public ReadOnlyBooleanProperty openedProperty() {
        return opened.getReadOnlyProperty();
    }

    @Override
    public ObservableList<Consumer<String>> getEventHandlers() {
        return eventHandlers;
    }

    @Override
    public boolean queueDataToSend(byte[] bytes, int delayAfter) {
        return dataToSend.offer(new QueuedRx(bytes,delayAfter));
    }

    @Override
    public ReadOnlyStringProperty interfaceNameProperty() {
        return name.getReadOnlyProperty();
    }
}
