package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 12/04/2015.
 */
public interface SectionEvent {
    void accept(SectionEventVisitor visitor);
}
