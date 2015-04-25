package org.beynet.jnote.controler;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteRef {
    private final String UUID;
    private final String name;
    private final NoteSectionRef noteSectionRef;

    public NoteRef(NoteSectionRef noteSectionRef,String UUID,String name) {
        this.UUID=UUID;
        this.name=name;
        this.noteSectionRef=noteSectionRef;
    }

    public NoteSectionRef getNoteSectionRef() {
        return noteSectionRef;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

}
