package org.beynet.jnote.model;

import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by beynet on 06/04/2015.
 */
public class NoteBook {
    public NoteBook(Path path) {
        this.path = path ;
        logger.debug("create new notebook name="+path.getFileName());
    }

    /**
     * add existing section by its path
     * @param filePath : the existing section
     * @return
     * @throws IllegalArgumentException
     */
    NoteSection addSection(Path filePath) throws IllegalArgumentException {
        logger.debug("add note section " + filePath.toString() + " to note book " + path.getFileName());
        NoteSection noteSection = NoteSection.fromZipFile(path.resolve(filePath));
        this.sections.add(noteSection);
        return noteSection;
    }

    /**
     * add a new section
     * @param sectionName
     * @return
     * @throws IllegalArgumentException
     */
    public NoteSection addSection(String sectionName) throws IllegalArgumentException {
        //TODO : check if no such section exist
        logger.debug("add note section name=" + sectionName + " to note book " + path.getFileName());
        NoteSection noteSection = NoteSection.fromZipFile(path.resolve(sectionName+".zip"));
        this.sections.add(noteSection);
        return noteSection;
    }

    public List<NoteSection> getSections() {
        return sections;
    }


    private Path path;
    private List<NoteSection> sections = new ArrayList<>();
    private final static Logger logger = Logger.getLogger(NoteBook.class);

}
