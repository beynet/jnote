package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public interface ModelEvent {
    void accept(ModelEventVisitor visitor);
}
