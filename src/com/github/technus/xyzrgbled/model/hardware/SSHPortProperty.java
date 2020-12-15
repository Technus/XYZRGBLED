package com.github.technus.xyzrgbled.model.hardware;

import com.jcraft.jsch.*;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SSHPortProperty extends SimpleObjectProperty<Channel> implements IHardwareCommunication {
    public static List<SSHPortProperty> generateConnection(){
        return Collections.emptyList();
    }

    public SSHPortProperty() {

    }

    @Override
    public void open() {

    }

    @Override
    public void close() {
        set(null);
    }

    @Override
    public ReadOnlyBooleanProperty openedProperty() {
        return null;
    }

    @Override
    public ObservableList<Consumer<String>> getEventHandlers() {
        return null;
    }

    @Override
    public boolean queueDataToSend(byte[] bytes, int delayAfter) {
        return false;
    }

    @Override
    public ReadOnlyStringProperty interfaceNameProperty() {
        return null;
    }
}
