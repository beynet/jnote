package org.beynet.jnote.model.events.note;

/**
 * Created by beynet on 08/04/15.
 */
public class AttachmentRemovedFromNote implements NoteEvent {
    private final String noteUUID;
    private final String fileName;

    public AttachmentRemovedFromNote(String noteUUID, String fileName) {
        this.noteUUID=noteUUID;
        this.fileName=fileName;
    }

    public String getNoteUUID() {
        return noteUUID;
    }
    public String getFileName() {
        return fileName;
    }

    @Override
    public void accept(NoteEventVisitor visitor) {
        visitor.visit(this);
    }

}
