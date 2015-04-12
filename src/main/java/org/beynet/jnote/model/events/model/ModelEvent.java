package org.beynet.jnote.model.events.model;

/**
 * Created by beynet on 12/04/2015.
 */
public interface ModelEvent {
    public void accept(ModelEventVisitor visitor);
}
