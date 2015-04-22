package org.beynet.jnote.model.events.note;

/**
 * Created by beynet on 22/04/2015.
 */
public interface NoteEventVisitor {

    void visit(AttachmentAddedToNote attachmentAddedToNote);

    void visit(AttachmentRemovedFromNote attachmentRemovedFromNote);
}
