package org.beynet.jnote.controler;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteBookRef {
    private final SimpleObjectProperty<String> name ;
    private final String UUID;

    public NoteBookRef(String UUID,String name) {
        this.UUID=UUID;
        this.name=new SimpleObjectProperty<>();
        this.name.set(name);
    }

    public String getName() {
        return name.getValue();
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public String toString() {
        return getName();
    }


    public static Callback<NoteBookRef, Observable[]> extractor() {
        return (NoteBookRef p) -> new Observable[]{p.name};
    }

    public void changeName(String current) {
        this.name.set(current);
    }
}
