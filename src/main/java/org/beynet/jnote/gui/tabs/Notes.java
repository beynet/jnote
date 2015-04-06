package org.beynet.jnote.gui.tabs;

import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beynet on 05/04/2015.
 */
public class Notes extends TabPane {
    public Notes() {
        this.addNoteTab = new AddNoteTab();
        createNewNote();
        setOnMouseClicked(event -> {
            checkClick(event);
        });
    }

    private void checkClick(MouseEvent event) {
        System.out.println("on click");
    }


    public void createNewNote() {
        getTabs().clear();
        mySections.add(new NoteSection(NEW_TITLE));
        for (NoteSection n : mySections) {
            getTabs().add(n);
        }
        getTabs().add(addNoteTab);
    }

    private AddNoteTab addNoteTab;
    private List<NoteSection> mySections = new ArrayList<>();

    private final static String NEW_TITLE = "new section";

}
