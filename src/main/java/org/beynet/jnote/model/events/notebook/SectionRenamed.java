package org.beynet.jnote.model.events.notebook;

import org.beynet.jnote.model.events.notebook.NoteBookEvent;
import org.beynet.jnote.model.events.notebook.NoteBookEventVisitor;

/**
 * Created by beynet on 07/04/15.
 */
public class SectionRenamed implements NoteBookEvent {

    private final String name;
    private final String UUID;

    public SectionRenamed(String UUID, String name) {
        this.UUID = UUID;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(NoteBookEventVisitor visitor) {
        visitor.visit(this);
    }
}
