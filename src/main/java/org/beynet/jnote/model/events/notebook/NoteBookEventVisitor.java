package org.beynet.jnote.model.events.notebook;

import org.beynet.jnote.model.events.model.NewNoteBookEvent;
import org.beynet.jnote.model.events.model.OnExitEvent;

/**
 * Created by beynet on 07/04/15.
 */
public interface NoteBookEventVisitor {
    void visit(NoteSectionAdded event);

    void visit(SectionRenamed sectionRenamed);

    void visit(NoteSectionDeleted noteSectionDeleted);
}
