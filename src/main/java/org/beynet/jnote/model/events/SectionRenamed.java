package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public class SectionRenamed implements ModelEvent {

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
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }
}
