package org.beynet.jnote.model.events.model;

import org.beynet.jnote.model.events.notebook.NoteBookEvent;
import org.beynet.jnote.model.events.notebook.NoteBookEventVisitor;

/**
 * Created by beynet on 07/04/15.
 */
public class OnExitEvent implements ModelEvent {
    public OnExitEvent() {

    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }
}
