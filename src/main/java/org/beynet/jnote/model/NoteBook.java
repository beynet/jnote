package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.beynet.jnote.model.events.notebook.NoteSectionAdded;
import org.beynet.jnote.model.events.notebook.NoteSectionDeleted;
import org.beynet.jnote.model.events.notebook.SectionRenamed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * NoteBook : this is mainly a list of {@link NoteSection} contained in a filesystem directory
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

    /**
     * @return current note notebook name, ie the directory name
     */
    public String getName() {
        return this.path.getFileName().toString();
    }

    /**
     * change current note book name
     * @param newName
     * @throws IOException
     */
    public void changeName(String newName) throws IOException {
        String previous = getName();
        logger.debug("changing notebook name from "+previous+" to "+newName);
        Path target = this.path.resolveSibling(newName);
        if (Files.exists(target)) throw new IOException("target file already exists");
        Files.move(this.path, target);
        this.path=target;
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

    public void saveNoteContent(String uuid, String noteUUID, String content, IndexWriter writer) throws IllegalArgumentException,IOException {
        NoteSection sectionByUUID = getSectionByUUID(uuid);
        sectionByUUID.saveNoteContent(getName(),noteUUID, content, writer);
    }
    public void undoNoteContent(String uuid, String noteUUID, IndexWriter writer,String content) throws IllegalArgumentException,IOException {
        NoteSection sectionByUUID = getSectionByUUID(uuid);
        sectionByUUID.undoNoteContent(getName(),noteUUID, writer,content);
    }



    public void changeSectionName(IndexWriter writer,String uuid, String name) throws IllegalArgumentException,IOException {
        final NoteSection section = getSectionByUUID(uuid);
        section.changeName(getName(),name,writer);
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
    public void adoptNote(String sectionUUID,Note note) throws IOException {
        getSectionByUUID(sectionUUID).adoptNote(note);
    }
    public void delNote(String sectionUUID,String noteUUID,IndexWriter writer) throws IOException {
        NoteSection section = getSectionByUUID(sectionUUID);
        section.delNote(noteUUID,writer);
    }
    public void delete(IndexWriter writer) throws IOException {
        synchronized (sectionsMap) {
            for (NoteSection section : sectionsMap.values()) {
                section.delete(writer);
                setChanged();
                notifyObservers(new NoteSectionDeleted(section.getUUID(),true));
            }
            sectionsMap.clear();
            Files.delete(path);
        }
        //Files.delete(path);
        // TODO notify notebook deleted
    }


    public String getNoteContent(String sectionUUID,String noteUUID) throws IOException {
        return getSectionByUUID(sectionUUID).getNoteContent(noteUUID);
    }


    public void addAttachment(String sectionUUID,String noteUUID, Path path) throws IOException, AttachmentAlreadyExistException {
        getSectionByUUID(sectionUUID).addNoteAttachment(noteUUID,path);
    }

    /**
     * subscribe to changes done to a note of the current notebook
     * @param sectionUUID
     * @param noteUUID
     * @param observer
     */
    public void subscribeToNote(String sectionUUID,String noteUUID, Observer observer) {
        getSectionByUUID(sectionUUID).subscribeToNote(noteUUID, observer);
    }

    /**
     * unsubscribe to changes done to a note of the current notebook
     * @param sectionUUID
     * @param noteUUID
     * @param observer
     */
    public void unSubscribeToNote(String sectionUUID,String noteUUID, Observer observer) {
        getSectionByUUID(sectionUUID).unSubscribeToNote(noteUUID, observer);
    }

    /**
     * remove an attachment from a note of current notebook
     * @param fileName
     * @param sectionUUID
     * @param noteUUID
     * @throws IOException
     * @throws AttachmentNotFoundException
     */
    public void deleteAttachment(String fileName,String sectionUUID,String noteUUID) throws IOException, AttachmentNotFoundException {
        getSectionByUUID(sectionUUID).deleteAttachment(fileName,noteUUID);
    }

    /**
     * save an attachment of a note to the target path
     * @param fileName
     * @param sectionUUID
     * @param noteUUID
     * @param path
     * @throws IOException
     * @throws AttachmentNotFoundException
     */
    public void saveAttachment(String fileName,String sectionUUID,String noteUUID, Path path) throws IOException, AttachmentNotFoundException {
        getSectionByUUID(sectionUUID).saveAttachment(fileName,noteUUID,path);
    }



    /**
     * reindex all the notes of the current note book
     * @param writer
     * @throws IOException
     */
    public void reIndexAllNotes(IndexWriter writer) throws IOException {
        synchronized (sectionsMap) {
            for (NoteSection noteSection : sectionsMap.values()) {
                noteSection.reIndexAllNotes(getName(),writer);
            }
        }
    }
    public void deleteSection(String sectionUUID,IndexWriter writer) throws IOException {
        getSectionByUUID(sectionUUID).delete(writer);
        sectionsMap.remove(sectionUUID);
        setChanged();
        notifyObservers(new NoteSectionDeleted(sectionUUID, true));
    }

    /**
     * move the note to the newSection
     * @param currentSectionUUID
     * @param noteUUID
     * @param newSection
     */
    public void moveNote(String currentSectionUUID, String noteUUID, NoteSection newSection,IndexWriter writer) throws IOException, AttachmentAlreadyExistException {
        getSectionByUUID(currentSectionUUID).moveNoteTo(newSection,noteUUID,writer);
    }

    private Path path;
    protected Map<String,NoteSection> sectionsMap = new HashMap<>();
    private final static Logger logger = Logger.getLogger(NoteBook.class);
    private final String UUID;

}
