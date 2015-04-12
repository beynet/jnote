package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.model.events.NoteRenamed;
import org.beynet.jnote.model.events.NoteSectionAdded;
import org.beynet.jnote.model.events.SectionRenamed;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * NoteBook : this is mainly a list of {@link NoteSection}
 */
public class NoteBook extends Observable {

    public NoteBook(Path path) {
        this.path = path ;
        logger.debug("create new notebook name="+path.getFileName());
        this.UUID = java.util.UUID.randomUUID().toString();
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return this.path.getFileName().toString();
    }

    @Override
    public synchronized void addObserver(Observer o) {
        logger.debug("add observer to notebook "+getName());
        super.addObserver(o);
        // we send to this new observer our section list
        synchronized (sectionsMap) {
            for (NoteSection section: sectionsMap.values()) {
                o.update(this,new NoteSectionAdded(section.getUUID(),section.getName()));
            }
        }
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        super.deleteObserver(o);
        logger.debug("delete observer to notebook "+getName());
    }

    /**
     *
     * @param section
     */
    private void addSection(NoteSection section) {
        sectionsMap.put(section.getUUID(), section);
        setChanged();
        //FIXME : here
        notifyObservers(new NoteSectionAdded(section.getUUID(), section.getName()));
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
        addSection(noteSection);
        return noteSection;
    }

    /**
     * add a new section
     * @param sectionName
     * @return
     * @throws IllegalArgumentException
     */
    private NoteSection _addSection(String sectionName) throws IllegalArgumentException, IOException {
        logger.debug("add note section name=" + sectionName + " to note book " + path.getFileName());
        NoteSection noteSection = NoteSection.fromZipFile(path.resolve(sectionName+".zip"));
        addSection(noteSection);
        noteSection.save();
        return noteSection;
    }

    /**
     * create a new section with provided name
     * @param sectionName
     * @return
     * @throws IllegalArgumentException : if section name already exists
     */
    public NoteSection addSection(String sectionName) throws IllegalArgumentException, IOException {
        synchronized (sectionsMap) {
            if (nameExist(sectionName)) throw new IllegalArgumentException("section name already exists");
            return _addSection(sectionName);
        }
    }


    private boolean nameExist(String name) {
        for (NoteSection section:sectionsMap.values()) {
            if (section.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * create a new section with default name
     * @return
     * @throws IllegalArgumentException
     */
    public NoteSection addSection() throws IllegalArgumentException, IOException {
        String newSectionName ="NEW SECTION";
        int i = 0;
        synchronized (sectionsMap) {
            while (nameExist(newSectionName)) {
                i++;
                newSectionName=newSectionName+i;
            }
            return _addSection(newSectionName);
        }
    }

    NoteSection getSectionByUUID(String uuid) throws IllegalArgumentException {
        final NoteSection section ;
        synchronized (sectionsMap) {
            section= sectionsMap.get(uuid);
        }
        if (section==null) throw new IllegalArgumentException("invalid section UUID");
        return section;
    }

    public void saveNoteContent(String uuid, String noteUUID, String content) throws IllegalArgumentException,IOException {
        getSectionByUUID(uuid).saveNoteContent(noteUUID,content);
    }

    public void changeSectionName(String uuid, String name) throws IllegalArgumentException,IOException {
        final NoteSection section = getSectionByUUID(uuid);
        section.changeName(name);
        setChanged();
        notifyObservers(new SectionRenamed(uuid,name));
    }

    public void changeNoteName(String sectionUUID, String noteUUID, String text) throws IllegalArgumentException,IOException {
        getSectionByUUID(sectionUUID).changeNoteName(noteUUID,text);
    }



    public void subscribeToNoteSection(String sectionUUID,Observer observer) throws IllegalArgumentException{
        getSectionByUUID(sectionUUID).addObserver(observer);
    }
    public void unSubscribeToNoteSection(String sectionUUID,Observer observer) throws IllegalArgumentException{
        getSectionByUUID(sectionUUID).deleteObserver(observer);
    }

    public void addNote(String sectionUUID) throws IOException {
        getSectionByUUID(sectionUUID).addNote();
    }
    public void delNote(NoteRef noteRef) throws IOException {
        NoteSection section = getSectionByUUID(noteRef.getNoteSectionRef().getUUID());
        section.delNote(noteRef);
    }

    private Path path;
    protected Map<String,NoteSection> sectionsMap = new HashMap<>();
    private final static Logger logger = Logger.getLogger(NoteBook.class);
    private final String UUID;

}
