package org.beynet.jnote.model;

import org.apache.log4j.Logger;

import java.nio.file.Path;

/**
 * Created by beynet on 06/04/2015.
 */
public class NoteBook {
    public NoteBook(String name) {
        this.name = name ;
        logger.debug("create new notebook name="+name);
    }

    public void addNoteSection(Path file) {
        logger.debug("add note section "+file.toString()+" to note book "+name);
    }


    private String name ;

    private final static Logger logger = Logger.getLogger(NoteBook.class);

}
