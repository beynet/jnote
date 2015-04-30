package org.beynet.jnote.gui.tabs;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.util.Callback;
import org.beynet.jnote.controler.NoteRef;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteListItem {
    public NoteListItem(NoteRef noteRef,boolean inEdition) {
        this.noteRef=noteRef;
        this.inEdition=new SimpleBooleanProperty();
        this.inEdition.set(Boolean.valueOf(inEdition));
        this.name=new SimpleStringProperty();
        this.name.set(noteRef.getName());
    }

    public NoteRef getNoteRef() {
        return noteRef;
    }

    public boolean isInEdition() {
        return inEdition.getValue().booleanValue();
    }

    public void setInEdition(boolean inEdition) {
        this.inEdition.set(Boolean.valueOf(inEdition));
    }

    public static Callback<NoteListItem, Observable[]> extractor() {
        return (NoteListItem p) -> new Observable[]{p.inEdition,p.name};
    }

    public void changeName(String name) {
        this.name.set(name);
    }

    public String getName() {
        return this.name.getValue();
    }


    BooleanProperty inEdition ;
    StringProperty name ;
    NoteRef noteRef;

}
