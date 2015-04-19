package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteAdded implements SectionEvent {

    private final String UUID;
    private final String name;

    public NoteAdded(String UUID,String name) {
        this.UUID=UUID;
        this.name=name;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(SectionEventVisitor visitor) {
        visitor.visit(this);
    }
}
