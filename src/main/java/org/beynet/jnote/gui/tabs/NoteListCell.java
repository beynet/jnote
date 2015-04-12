package org.beynet.jnote.gui.tabs;

import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteRef;

import java.io.IOException;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteListCell extends ListCell<NoteListItem> {
    public NoteListCell() {

    }

    @Override
    protected void updateItem(NoteListItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (item!=null && empty==false) {
            if (item.isInEdition()) {
                final TextField textField = new TextField(item.getName());
                setGraphic(textField);
                textField.setOnAction(event -> {
                    final NoteRef noteRef = item.getNoteRef();
                    item.setInEdition(false);
                    try {
                        Controller.changeNoteName(noteRef.getNoteSectionRef(), noteRef.getUUID(), textField.getText());
                    } catch (IOException e) {
                        //TODO : show an alert
                    }
                });
            } else {
                setText(item.getName());
            }
        }
    }
}
