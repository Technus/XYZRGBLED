package com.github.technus.xyzrgbled.model.hardware;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IHardwareCommunication {
    List<Supplier<List<? extends IHardwareCommunication>>> suppliers=new ArrayList<>();
    static void update(ObservableList<IHardwareCommunication> items) {
        items.clear();
        suppliers.forEach(iHardwareCommunicationSupplier -> items.addAll(iHardwareCommunicationSupplier.get()));
    }
    void close();
    void open();
    ReadOnlyBooleanProperty openedProperty();
    default boolean isOpened(){
        return openedProperty().get();
    }
    ObservableList<Consumer<String>> getEventHandlers();
    boolean queueDataToSend(byte[] bytes,int delayAfter);
    default boolean queueDataToSend(byte[] bytes){
        return queueDataToSend(bytes,0);
    }
    ReadOnlyStringProperty interfaceNameProperty();
    default String getInterfaceName(){
        return interfaceNameProperty().getValueSafe();
    }
}
