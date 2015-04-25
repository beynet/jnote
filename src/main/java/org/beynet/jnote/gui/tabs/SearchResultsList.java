package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.dialogs.Alert;

/**
 * Created by beynet on 25/04/2015.
 */
public class SearchResultsList extends ListView<NoteRef> {

    public class SearchResultsListCell extends ListCell<NoteRef> {
        @Override
        protected void updateItem(NoteRef item, boolean empty) {
            super.updateItem(item, empty);
            if (item==null ||empty==true) {
                setGraphic(null);
                setText(null);
            }
            else if (item!=null && empty==false) {
                setText(item.getNoteSectionRef().getNoteBookRef().getName()+" / "+
                        item.getNoteSectionRef().getSectionName()+" / "+
                        item.getName()
                );
            }
        }
    }

    public SearchResultsList(Stage currentStage, ComboBox<NoteBookRef> noteBooks, NoteBook notes, Tooltip searchTooltip) {
        setCellFactory(param -> {
            return new SearchResultsListCell();
        });

        setOnMouseClicked(event -> {
            if (event.getClickCount()==2) {
                searchTooltip.hide();
                NoteRef ref = getSelectionModel().getSelectedItem();
                if (ref!=null) {
                    for (NoteBookRef noteBookRef : noteBooks.getItems()) {
                        if (noteBookRef.getName().equals(ref.getNoteSectionRef().getNoteBookRef().getName())) {
                            noteBooks.getSelectionModel().select(noteBookRef);
                            Platform.runLater(() -> {
                                notes.selectSectionAndNote(ref);
                            });
                            break;
                        }
                    }
                }
            }
        });
    }
}
