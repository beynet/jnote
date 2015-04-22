package org.beynet.jnote.controler;

/**
 * Created by beynet on 22/04/2015.
 */
public class AttachmentRef {
    public AttachmentRef(NoteRef noteRef, String fileName, long size) {
        this.noteRef = noteRef;
        this.fileName = fileName;
        this.size=size;
    }

    public NoteRef getNoteRef() {
        return noteRef;
    }

    public String getFileName() {
        return fileName;
    }
    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return getFileName();
    }

    private NoteRef noteRef ;
    private String  fileName;
    private long size;

}
