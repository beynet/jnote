package org.beynet.jnote.model.events.notebook;

/**
 * Created by beynet on 07/04/15.
 */
public interface NoteBookEvent {
    void accept(NoteBookEventVisitor visitor);
}
