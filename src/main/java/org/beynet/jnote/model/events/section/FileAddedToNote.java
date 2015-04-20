package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 08/04/15.
 */
public class FileAddedToNote implements SectionEvent {
    private final String sectionUUID;
    private final String noteUUID;
    private final String fileName;

    public FileAddedToNote(String sectionUUID, String noteUUID, String fileName) {
        this.sectionUUID=sectionUUID;
        this.noteUUID=noteUUID;
        this.fileName=fileName;
    }

    public String getSectionUUID() {
        return sectionUUID;
    }

    public String getNoteUUID() {
        return noteUUID;
    }

    @Override
    public void accept(SectionEventVisitor visitor) {
        visitor.visit(this);
    }
    public String getFileName() {
        return fileName;
    }

}
