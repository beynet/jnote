package org.beynet.jnote.model.events;

/**
 * Created by beynet on 07/04/15.
 */
public interface ModelEventVisitor {
    void visit(NoteSectionAdded event);

    void visit(NewNoteBookEvent newNoteBookEvent);

    void visit(SectionRenamed sectionRenamed);

    void visit(OnExitEvent onExitEvent);

    void visit(NoteAdded noteAdded);

    void visit(NoteRenamed noteRenamed);

    void visit(NoteContentChanged noteContentChanged);

    void visit(NoteDeleted noteDeleted);
}
