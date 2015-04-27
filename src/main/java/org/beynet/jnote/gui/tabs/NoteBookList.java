package org.beynet.jnote.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.beynet.jnote.controler.NoteBookRef;

/**
 * Created by beynet on 27/04/2015.
 */
public class NoteBookList extends ComboBox<NoteBookRef> {

    public class NoteBookListCell extends ListCell<NoteBookRef> {
        @Override
        protected void updateItem(NoteBookRef item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item!=null && empty==false) {
                setText(item.getName());
            }
        }
    }


    public NoteBookList(Stage currentStage,NoteBook notes) {
        values = FXCollections.observableArrayList(NoteBookRef.extractor());
        setItems(values);
        this.selected = null;
        setCellFactory(param -> {
            return new NoteBookListCell();
        });

        setOnAction(event -> {
            selected = getValue();
            notes.changeCurrentNoteBook(selected);
        });
    }

    public NoteBookRef getSelected() {
        return selected;
    }

    private NoteBookRef selected;
    private ObservableList<NoteBookRef> values ;

}
