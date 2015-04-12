package org.beynet.jnote.model.events.model;

/**
 * Created by beynet on 12/04/2015.
 */
public interface ModelEventVisitor {
    void visit(NewNoteBookEvent newNoteBookEvent);

    void visit(OnExitEvent onExitEvent);
}
