package org.beynet.jnote.controler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beynet on 07/04/15.
 */
public class NoteSectionRef {
    private final NoteBookRef noteBookRef;
    private final String UUID;
    private final String sectionName;



    public NoteSectionRef(NoteBookRef noteBookRef,String UUID,String sectionName){
        this.noteBookRef=noteBookRef;
        this.UUID =UUID;
        this.sectionName=sectionName;
    }

    public NoteBookRef getNoteBookRef() {
        return noteBookRef;
    }

    public String getUUID() {
        return UUID;
    }

    public String getSectionName() {
        return sectionName;
    }

    @Override
    public String toString() {
        return getSectionName();
    }
}
