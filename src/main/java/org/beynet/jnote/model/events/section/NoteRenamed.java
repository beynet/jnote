package org.beynet.jnote.model.events.section;

/**
 * Created by beynet on 08/04/15.
 */
public class NoteRenamed implements SectionEvent {
    private final String sectionUUID;
    private final String noteUUID;
    private final String name;

    public NoteRenamed(String sectionUUID,String noteUUID,String name) {
        this.sectionUUID=sectionUUID;
        this.noteUUID=noteUUID;
        this.name=name;
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
    public String getName() {
        return name;
    }

}
