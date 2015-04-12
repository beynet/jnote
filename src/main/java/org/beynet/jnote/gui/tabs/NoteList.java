package org.beynet.jnote.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.utils.I18NHelper;

import java.io.IOException;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteList extends ListView<NoteListItem> {
    public NoteList(Stage currentStage,NoteSectionRef noteSectionRef) {
        this.currentStage= currentStage;
        list = FXCollections.observableArrayList(NoteListItem.extractor());
        setItems(list);

        ctxMenu = new ContextMenu();
        final MenuItem addNewNote = new MenuItem(I18NHelper.getLabelResourceBundle().getString("addNote"));
        final MenuItem delNote = new MenuItem(I18NHelper.getLabelResourceBundle().getString("delNote"));
        ctxMenu.getItems().add(addNewNote);
        ctxMenu.getItems().add(delNote);

        setCellFactory(param -> {
            NoteListCell result = new NoteListCell();
            result.setOnMouseClicked(event -> {
                ctxMenu.hide();
                if (MouseButton.SECONDARY.equals(event.getButton())) {
                    if (result.isEmpty() == false) {
                        delNote.setDisable(false);
                    } else {
                        delNote.setDisable(true);
                    }
                    ctxMenu.show(this, event.getScreenX(), event.getScreenY());
                } else if (MouseButton.PRIMARY.equals(event.getButton())) {
                    if (event.getClickCount() == 2) {
                        NoteListItem item = result.getItem();
                        if (item != null) {
                            item.setInEdition(true);
                        }
                    }
                }
            });
            return result;
        });

        addNewNote.setOnAction(event -> {
            try {
                Controller.addNote(noteSectionRef);
            } catch (IOException e) {
                new Alert(currentStage, "unable to create note :" + e.getMessage()).show();
            }
        });

        delNote.setOnAction(event->{
            NoteListItem item = getSelectionModel().getSelectedItem();
            if (item!=null) {
                try {
                    Controller.delNote(item.getNoteRef());
                } catch (IOException e) {
                    new Alert(currentStage,"unable to delete note "+e.getMessage());
                }
            }
        });

    }


    public ObservableList<NoteListItem> getList() {
        return list;
    }

    private ObservableList<NoteListItem> list ;
    private Stage currentStage;
    private ContextMenu ctxMenu;

}
