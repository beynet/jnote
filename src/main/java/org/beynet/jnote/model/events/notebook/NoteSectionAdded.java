package org.beynet.jnote.model.events.notebook;

import org.beynet.jnote.model.events.notebook.NoteBookEvent;
import org.beynet.jnote.model.events.notebook.NoteBookEventVisitor;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteSectionAdded implements NoteBookEvent {
    private final String UUID;
    private final String name;

    public NoteSectionAdded(String UUID,String name) {
        this.UUID = UUID;
        this.name = name ;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(NoteBookEventVisitor visitor) {
        visitor.visit(this);
    }


}
