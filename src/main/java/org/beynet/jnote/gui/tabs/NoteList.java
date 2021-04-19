package org.beynet.jnote.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.gui.dialogs.MoveNote;
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
        final MenuItem moveNote = new MenuItem(I18NHelper.getLabelResourceBundle().getString("moveNote"));
        ctxMenu.getItems().add(addNewNote);
        ctxMenu.getItems().add(delNote);
        ctxMenu.getItems().add(moveNote);

        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.oldSelected = oldValue;
            if (this.oldSelected!=null) {
                if (Boolean.TRUE.equals(this.oldSelected.inEdition.getValue())) this.oldSelected.setInEdition(false);
            }
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                oldSelected=null;
            }
        });

        setCellFactory(param -> {
            NoteListCell result = new NoteListCell();
            result.setOnMouseClicked(event -> {
                ctxMenu.hide();
                if (MouseButton.SECONDARY.equals(event.getButton())) {
                    if (result.isEmpty() == false) {
                        delNote.setDisable(false);
                        moveNote.setDisable(false);
                    } else {
                        delNote.setDisable(true);
                        moveNote.setDisable(true);
                    }
                    ctxMenu.show(this, event.getScreenX(), event.getScreenY());
                } else if (MouseButton.PRIMARY.equals(event.getButton())) {
                    if (isFocused()==true) {
                        if (event.getClickCount() == 1) {
                            NoteListItem item = result.getItem();
                            if (item != null && item == getSelectionModel().getSelectedItem()) {
                                if (item != null && item == oldSelected) {
                                    item.setInEdition(true);
                                    oldSelected=null;
                                }
                                else {
                                    oldSelected = item;
                                }
                            }
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
                new Alert(currentStage, I18NHelper.getLabelResourceBundle().getString("errorCreatingNote") + e.getMessage(),e).show();
            }
        });
        moveNote.setOnAction(event->{
            NoteListItem item = getSelectionModel().getSelectedItem();
            if (item!=null) {
                MoveNote moveNote1 = new MoveNote(currentStage, Double.valueOf(500), Double.valueOf(200), item.getNoteRef());
                moveNote1.show();
            }
        });

        delNote.setOnAction(event->{
            NoteListItem item = getSelectionModel().getSelectedItem();
            if (item!=null) {
                try {
                    Controller.delNote(item.getNoteRef());
                } catch (IOException e) {
                    new Alert(currentStage,I18NHelper.getLabelResourceBundle().getString("errorDeletingNote")+e.getMessage(),e);
                }
            }
        });

    }


    public ObservableList<NoteListItem> getList() {
        return list;
    }

    public boolean containsNote(String uuid) {
        for (NoteListItem item : list) {
            if (item.getNoteRef().getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private ObservableList<NoteListItem> list ;
    private Stage currentStage;
    private ContextMenu ctxMenu;
    private NoteListItem oldSelected = null;

}
