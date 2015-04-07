package org.beynet.jnote.controler;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteBookRef {
    private final String name;
    private final String UUID;

    public NoteBookRef(String UUID,String name) {
        this.UUID=UUID;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public String toString() {
        return getName();
    }
}
