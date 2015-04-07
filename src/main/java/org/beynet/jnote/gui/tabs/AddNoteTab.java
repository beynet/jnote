package org.beynet.jnote.gui.tabs;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Created by beynet on 05/04/2015.
 */
public class AddNoteTab extends Tab {
    public AddNoteTab() {
        setGraphic(new Label("+"));
        setOnSelectionChanged(event -> {
            if (isSelected() == true) {
                final TabPane tabPane = getTabPane();
                if (tabPane!=null) ((NoteBook) tabPane).createNewNoteSection();
            }
        });
    }

}
