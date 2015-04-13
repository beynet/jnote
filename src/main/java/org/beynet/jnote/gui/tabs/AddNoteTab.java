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
            if (isSelected() == true && skipNextSelectionChange==true) {
                final TabPane tabPane = getTabPane();
                if (tabPane!=null) ((NoteBook) tabPane).createNewNoteSection();
            }
            skipNextSelectionChange=false;
        });
    }

    /**
     * when a section is removed this tab should be selected after removing current section
     * we do not want to create a new section in this case
     */
    public void skipNextSelectionChange() {
        skipNextSelectionChange=true;
    }

    boolean skipNextSelectionChange = false;
}
