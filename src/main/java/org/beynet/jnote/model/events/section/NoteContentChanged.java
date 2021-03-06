package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteContentChanged implements SectionEvent {
    private final String sectionUUID;
    private final String noteUUID;
    private final String content;

    public NoteContentChanged(String sectionUUID, String noteUUID, String content) {
        this.sectionUUID=sectionUUID;
        this.noteUUID=noteUUID;
        this.content=content;
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
    public String getContent() {
        return content;
    }

}
