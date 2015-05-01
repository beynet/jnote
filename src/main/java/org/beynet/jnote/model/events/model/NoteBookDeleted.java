package org.beynet.jnote.model.events.model;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteBookDeleted implements ModelEvent {

    public NoteBookDeleted(String UUID) {
        this.UUID = UUID;
    }


    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }

    private String UUID ;
}
