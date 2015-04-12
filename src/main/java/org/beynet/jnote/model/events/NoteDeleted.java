package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteDeleted implements ModelEvent {

    private final String UUID;

    public NoteDeleted(String UUID) {
        this.UUID=UUID;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }
}
