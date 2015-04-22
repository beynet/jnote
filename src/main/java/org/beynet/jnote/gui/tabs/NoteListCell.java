package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.dialogs.Alert;

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
                        logger.error("unable to change note name",e);
//                        Platform.runLater(()-> {
//                            new Alert("unable to change note name " + e.getMessage()).show();
//                        });
                    }
                });
            } else {
                setText(item.getName());
            }
        }
    }

    private final static Logger logger = Logger.getLogger(NoteListCell.class);
}
