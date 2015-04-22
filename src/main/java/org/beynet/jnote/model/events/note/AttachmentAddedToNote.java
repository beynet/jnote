package org.beynet.jnote.model.events.note;

/**
 * Created by beynet on 08/04/15.
 */
public class AttachmentAddedToNote implements NoteEvent {
    private final String noteUUID;
    private final String fileName;
    private final long size ;

    public AttachmentAddedToNote(String noteUUID, String fileName,long size) {
        this.noteUUID=noteUUID;
        this.fileName=fileName;
        this.size= size;
    }

    public String getNoteUUID() {
        return noteUUID;
    }
    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    @Override
    public void accept(NoteEventVisitor visitor) {
        visitor.visit(this);
    }

}
