package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * Created by beynet on 05/04/2015.
 */
public class AddNoteTab extends NoteSection {
    public AddNoteTab() {
        super("+");
        setOnSelectionChanged(event -> {
            if (isSelected()==true) {
                ((Notes) getTabPane()).createNewNote();
            }
        });
    }

}
