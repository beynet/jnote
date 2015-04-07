package org.beynet.jnote.model.events;

import org.beynet.jnote.controler.NoteSectionRef;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteSectionAdded implements ModelEvent {
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
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }


}
