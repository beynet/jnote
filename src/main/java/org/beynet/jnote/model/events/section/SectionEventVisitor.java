package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 12/04/2015.
 */
public interface SectionEventVisitor {
    void visit(NoteDeleted noteDeleted);
    void visit(NoteAdded noteAdded);
    void visit(NoteRenamed noteRenamed);
    void visit(NoteContentChanged noteContentChanged);
    void visit(FileAddedToNote fileAddedToNote);
}
