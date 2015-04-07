package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteAdded implements ModelEvent {

    private final String UUID;
    private final String name;
    private final String content;

    public NoteAdded(String UUID,String name,String content) {
        this.UUID=UUID;
        this.name=name;
        this.content=content;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }
}
