package org.beynet.jnote.model.events.notebook;

import org.beynet.jnote.model.events.notebook.NoteBookEvent;
import org.beynet.jnote.model.events.notebook.NoteBookEventVisitor;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteSectionDeleted implements NoteBookEvent {
    private final String UUID;
    private boolean removeBook;

    public NoteSectionDeleted(String UUID,boolean removeBook) {
        this.UUID = UUID;
        this.removeBook = removeBook;
    }

    public boolean isRemoveBook() {
        return this.removeBook;
    }

    public String getUUID() {
        return UUID;
    }


    @Override
    public void accept(NoteBookEventVisitor visitor) {
        visitor.visit(this);
    }


}
