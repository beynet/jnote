package org.beynet.jnote.model.events.model;

import org.beynet.jnote.model.events.notebook.NoteBookEvent;
import org.beynet.jnote.model.events.notebook.NoteBookEventVisitor;

/**
 * Created by beynet on 07/04/15.
 */
public class NewNoteBookEvent implements ModelEvent {

    public NewNoteBookEvent(String UUID,String name) {
        this.name = name ;
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }

    private String name ;
    private String UUID ;
}
