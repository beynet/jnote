package org.beynet.jnote.controler;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteRef {
    private final String UUID;
    private final String name;
    private String content;
    private final NoteSectionRef noteSectionRef;

    public NoteRef(NoteSectionRef noteSectionRef,String UUID,String name,String content) {
        this.UUID=UUID;
        this.name=name;
        this.content=content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
