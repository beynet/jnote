package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.beynet.jnote.model.events.notebook.NoteSectionAdded;
import org.beynet.jnote.model.events.notebook.NoteSectionDeleted;
import org.beynet.jnote.model.events.notebook.SectionRenamed;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * NoteBook : this is mainly a list of {@link NoteSection}
 */
public class NoteBook extends Observable {

    public NoteBook(Path path) {
        this.path = path ;
        this.UUID = java.util.UUID.randomUUID().toString();
        logger.debug("create new notebook name="+this.path.getFileName()+" UUID="+this.UUID);
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
        logger.debug("delete observer from notebook " + getName());
    }

    /**
     * add section to map and notify observers
     * @param section
     */
    private void addSectionToMap(NoteSection section) {
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
    NoteSection addExistingSection(Path filePath) throws IllegalArgumentException {
        logger.debug("add existing note section path=" + filePath.toString() + " to note book " + path.getFileName());
        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(path.resolve(filePath));
        addSectionToMap(noteSection);
        return noteSection;
    }

    /**
     * create a new section and add it to current map
     * @param sectionName
     * @return
     * @throws IllegalArgumentException
     */
    private NoteSection _addSection(String sectionName) throws IllegalArgumentException, IOException {
        logger.debug("add new note section name=" + sectionName + " to note book " + path.getFileName());
        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(path.resolve(sectionName + ".zip"));
        addSectionToMap(noteSection);
        noteSection.save();
        return noteSection;
    }

    /**
     * create a new section with provided name
     * @param sectionName
     * @return
     * @throws IllegalArgumentException : if section name already exists
     */
    public NoteSection createNewEmptySection(String sectionName) throws IllegalArgumentException, IOException {
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
    public NoteSection createNewEmptySection() throws IllegalArgumentException, IOException {
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
        notifyObservers(new SectionRenamed(uuid, name));
    }

    public void changeNoteName(String sectionUUID, String noteUUID, String text) throws IllegalArgumentException,IOException {
        getSectionByUUID(sectionUUID).changeNoteName(noteUUID, text);
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
        section.delNote(noteRef.getUUID());
    }
    public void delete() throws IOException {
        synchronized (sectionsMap) {
            for (NoteSection section : sectionsMap.values()) {
                section.delete();
                setChanged();
                notifyObservers(new NoteSectionDeleted(section.getUUID(),true));
            }
            sectionsMap.clear();
        }
        //Files.delete(path);
        // TODO notify notebook deleted
    }


    public String getNoteContent(NoteRef noteRef) throws IOException {
        return getSectionByUUID(noteRef.getNoteSectionRef().getUUID()).getNoteContent(noteRef.getUUID());
    }


    public void addAttachment(NoteRef noteRef, Path path) throws IOException, AttachmentAlreadyExistException {
        getSectionByUUID(noteRef.getNoteSectionRef().getUUID()).addNoteAttachment(noteRef.getUUID(),path);
    }

    public void subscribeToNote(NoteRef noteRef, Observer observer) {
        getSectionByUUID(noteRef.getNoteSectionRef().getUUID()).subscribeToNote(noteRef.getUUID(), observer);
    }


    public void unSubscribeToNote(NoteRef noteRef, Observer observer) {
        getSectionByUUID(noteRef.getNoteSectionRef().getUUID()).unSubscribeToNote(noteRef.getUUID(), observer);
    }
    public void deleteAttachment(AttachmentRef attachmentRef) throws IOException, AttachmentNotFoundException {
        getSectionByUUID(attachmentRef.getNoteRef().getNoteSectionRef().getUUID()).deleteAttachment(attachmentRef);
    }
    public void saveAttachment(AttachmentRef attachmentRef, Path path) throws IOException, AttachmentNotFoundException {
        getSectionByUUID(attachmentRef.getNoteRef().getNoteSectionRef().getUUID()).saveAttachment(attachmentRef,path);
    }

    private Path path;
    protected Map<String,NoteSection> sectionsMap = new HashMap<>();
    private final static Logger logger = Logger.getLogger(NoteBook.class);
    private final String UUID;

}
