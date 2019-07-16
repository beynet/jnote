package org.beynet.jnote.gui.dialogs;

import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteRef;

public class MoveNote extends DialogModal {
    public MoveNote(Stage parent, Double width, Double height, NoteRef note) {
        super(parent, width, height);
        this.note = note ;
        //Controller.subscribeToModel();
    }



    private NoteRef note;
}
