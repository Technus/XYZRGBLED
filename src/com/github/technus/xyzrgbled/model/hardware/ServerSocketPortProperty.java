package com.github.technus.xyzrgbled.model.hardware;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ServerSocketPortProperty extends SimpleObjectProperty<ServerSocket> implements IHardwareCommunication {
    public static List<ServerSocketPortProperty> generateConnection(){
        return Collections.singletonList(new ServerSocketPortProperty());
    }

    private final ReadOnlyStringWrapper name=new ReadOnlyStringWrapper("ServerSocket");
    private final ReadOnlyBooleanWrapper opened =new ReadOnlyBooleanWrapper();
    private final ObservableList<Consumer<String>> eventHandlers= FXCollections.observableArrayList();
    private final Queue<QueuedRx> dataToSend=new ConcurrentLinkedQueue<>();
    private Thread writerRunner,reader;
    private Socket socket;

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
            socket.close();
        } catch (NullPointerException | IOException e) {
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
            set(new ServerSocket(9001));
            writerRunner = new Thread(() -> {
                while (get().isBound() || !Thread.currentThread().isInterrupted()) {
                    try {
                        socket = get().accept();
                    } catch (IOException e) {
                        close();
                        return;
                    }
                    name.set(socket.getInetAddress().getHostAddress());
                    InputStreamReader inputStreamReader = null;
                    OutputStream outputStreamWriter = null;
                    try {
                        inputStreamReader = new InputStreamReader(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStreamWriter = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (outputStreamWriter != null && inputStreamReader != null) {
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        reader = new Thread(() -> {
                            while (socket.isConnected() || !Thread.currentThread().isInterrupted()) {
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
                        while (socket.isConnected() || !Thread.currentThread().isInterrupted()) {
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
                        name.set("ServerSocket :(");
                    }
                    try {
                        socket.close();
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
