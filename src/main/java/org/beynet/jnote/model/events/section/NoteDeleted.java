package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteDeleted implements SectionEvent {

    private final String UUID;

    public NoteDeleted(String UUID) {
        this.UUID=UUID;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(SectionEventVisitor visitor) {
        visitor.visit(this);
    }
}
