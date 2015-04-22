package org.beynet.jnote.model.events.note;

/**
 * Created by beynet on 12/04/2015.
 */
public interface NoteEvent {
    void accept(NoteEventVisitor visitor);
}
