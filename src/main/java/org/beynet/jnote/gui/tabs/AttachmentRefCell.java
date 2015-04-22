package org.beynet.jnote.gui.tabs;

import javafx.scene.control.ListCell;
import org.beynet.jnote.controler.AttachmentRef;

/**
 * Created by beynet on 22/04/2015.
 */
public class AttachmentRefCell extends ListCell<AttachmentRef> {
    @Override
    protected void updateItem(AttachmentRef item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (item!=null && empty==false) {
            setText(item.getFileName());
        }
    }
}
