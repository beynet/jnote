package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public class OnExitEvent implements ModelEvent {
    public OnExitEvent() {

    }

    @Override
    public void accept(ModelEventVisitor visitor) {
        visitor.visit(this);
    }
}
