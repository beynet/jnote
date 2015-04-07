package org.beynet.jnote.controler;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteRef {
    private final String UUID;
    private final String name;
    private final String content;

    public NoteRef(String UUID,String name,String content) {
        this.UUID=UUID;
        this.name=name;
        this.content=content;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
