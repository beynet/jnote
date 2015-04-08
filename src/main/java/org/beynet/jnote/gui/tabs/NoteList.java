package org.beynet.jnote.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteSectionRef;

import java.io.IOException;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteList extends ListView<NoteListItem> {
    public NoteList(NoteSectionRef noteSectionRef) {
        list = FXCollections.observableArrayList(NoteListItem.extractor());
        setItems(list);

        ctxMenu = new ContextMenu();
        final MenuItem addNewNote = new MenuItem("add new note");
        ctxMenu.getItems().add(addNewNote);

        setCellFactory(param -> new NoteListCell());
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                NoteListItem item = getSelectionModel().getSelectedItem();
                if (item != null) {
                    item.setInEdition(true);
                }
            }
            else if (MouseButton.SECONDARY.equals(event.getButton())) {
                ctxMenu.show(this,event.getScreenX(),event.getScreenY());
            }
        });

        addNewNote.setOnAction(event -> {
            try {
                Controller.addNote(noteSectionRef);
            } catch (IOException e) {
                //TODO : show an alert
            }
        });

    }


    public ObservableList<NoteListItem> getList() {
        return list;
    }

    private ObservableList<NoteListItem> list ;
    private ContextMenu ctxMenu;

}
