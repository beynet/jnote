package org.beynet.jnote.model.events.model;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteBookRenamed implements ModelEvent {

    public NoteBookRenamed(String UUID,String current) {
        this.current=current;
        this.UUID = UUID;
    }


    public String getCurrent() {
        return current;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }

    private String current ;
    private String UUID ;
}
