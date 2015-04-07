package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public class SectionModifiedOrCreated implements ModelEvent {
    public SectionModifiedOrCreated(String UUID, String name, String content) {
        this.UUID    = UUID;
        this.name    = name;
        this.content = content;
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

    private String UUID    ;
    private String name    ;
    private String content ;
}
