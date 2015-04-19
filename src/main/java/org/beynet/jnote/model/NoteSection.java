package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.model.events.section.NoteAdded;
import org.beynet.jnote.model.events.section.NoteContentChanged;
import org.beynet.jnote.model.events.section.NoteDeleted;
import org.beynet.jnote.model.events.section.NoteRenamed;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A note section is a list of notebook
 */
@XmlRootElement(name = "NoteSection")
public class NoteSection extends Observable {
    NoteSection() {
        this.UUID = java.util.UUID.randomUUID().toString();
        this.modified=this.created=System.currentTimeMillis();
    }

    @Override
    public synchronized void addObserver(Observer o) {
        logger.debug("add observer to section "+getName());
        super.addObserver(o);
        for (NoteRef note : getNoteReferences()) {
            o.update(this,new NoteAdded(note.getUUID(),note.getName()));
        }
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        logger.debug("remove observer to section "+getName());
        super.deleteObserver(o);
    }

    @XmlElement(name="UUID")
    public String getUUID() {
        return UUID;
    }
    protected void setUUID(String UUID) {
        this.UUID=UUID;
    }

    @XmlAttribute(name="created")
    public long getCreated() {
        return created;
    }
    protected void setCreated(long created) {
        this.created=created;
    }

    @XmlElement(name="note")
    List<NoteRef> getNoteReferences(){
        return notes;
    }


    @XmlAttribute(name="modified")
    public long getModified() {
        return modified;
    }
    public void setModified(long modified) {
        this.modified = modified;
    }


    private static Optional<NoteSection> fromInputSource(InputSource source)  {
        Optional<NoteSection> result = Optional.empty();
        final Unmarshaller unmarshaller;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
            Object unmarshal = unmarshaller.unmarshal(source);
            if (unmarshal != null && unmarshal instanceof NoteSection) {
                result = Optional.of((NoteSection) unmarshal);
            }
        } catch(JAXBException e) {
            logger.error("unable to unmarshal string content",e);
        }
        return result ;
    }

    private Marshaller createMarshaller() {
        try {
            return jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("unable to construct marshaller",e);
        }
    }

    public synchronized void save() throws IOException {
        long modified = System.currentTimeMillis();
        setModified(modified);
        final Marshaller marshaller = createMarshaller();
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(),true)) {
            saveSection(fileSystem, marshaller);
        }
    }

    /**
     * change current section name
     * @param newName
     * @throws IOException
     */
    public void changeName(String newName) throws IOException {
        Path newPath = path.getParent().resolve(newName + ".zip");
        Files.move(path, newPath);
        path=newPath;
    }

    /**
     * @param zipAbsoluteFilePath
     * @return the content of the zip as a filesystem
     * @throws IOException
     */
    private static FileSystem getZipFileSystem(Path zipAbsoluteFilePath,boolean write) throws IOException {
        final URI uri = URI.create("jar:" + zipAbsoluteFilePath.toUri().toString());
        final Map<String, String> env = new HashMap<>();
        if (write==true && !Files.exists(zipAbsoluteFilePath)){
            env.put("create","true");
        }
        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * construct a new note section. if the provided file exist the section is loaded or an empty section is created
     * @param zipAbsoluteFilePath : the absolute file path where the section will be stored
     * @return
     * @throws IllegalArgumentException
     */
    public static NoteSection fromAbsoluteZipFilePath(Path zipAbsoluteFilePath) throws IllegalArgumentException {
        NoteSection result = new NoteSection();
        if (Files.exists(zipAbsoluteFilePath)) {

            try (FileSystem fileSystem = getZipFileSystem(zipAbsoluteFilePath,false)) {
                Path path = fileSystem.getPath(NOTE_SECTION_FILE_NAME);
                if (!Files.exists(path)) throw new IllegalArgumentException("zip does not contain section xml file");
                InputSource source = new InputSource();
                try (InputStream is = Files.newInputStream(path)){
                    source.setByteStream(is);
                    source.setEncoding("UTF-8");
                    Optional<NoteSection> opt = fromInputSource(source);
                    if (opt.isPresent()) result = opt.get();
                }
            } catch (IOException e) {
                logger.error("error reading zip", e);
            }
        }
        result.path = zipAbsoluteFilePath;
        if (result.getNoteReferences().size()==0) {
            try {
                result.addNote();
            } catch (IOException e) {
                logger.error("unable to add a note",e);
            }
        }
        return result;
    }

    @XmlTransient
    public Path getPath() {
        return path;
    }


    @XmlTransient
    public String getName() {
        String fileName = path.getFileName().toString();
        int i = fileName.indexOf(".");
        if (i==-1) return fileName;
        else return fileName.substring(0,i);
    }

    private NoteRef getNoteRefByUUID(String UUID) throws IllegalArgumentException {
        NoteRef found = null ;
        for (NoteRef ref : notes) {
            if (ref.getUUID().equals(UUID)) {
                found = ref;
                break;
            }
        }
        if (found==null) throw new IllegalArgumentException("note not found (UUID="+UUID+")");
        return found;
    }

    /**
     * read an return the expected note
     * @param UUID
     * @return : the note found (will nether be null)
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public synchronized Note readNote(String UUID) throws IOException,IllegalArgumentException {
        //check if the note exists
        getNoteRefByUUID(UUID);

        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(),false)) {
            Path path = fileSystem.getPath(UUID + ".xml");
            InputSource source = new InputSource();
            source.setEncoding("UTF-8");
            source.setByteStream(Files.newInputStream(path));
            Note note = (Note)jaxbContext.createUnmarshaller().unmarshal(source);
            return note;
        } catch (JAXBException e) {
            throw new IOException("unable to read note",e);
        }
    }

    /**
     * save the section
     * @param fileSystem : the zip file system
     * @param marshaller
     * @throws IOException
     */
    private void saveSection(FileSystem fileSystem,final Marshaller marshaller) throws IOException {
        Path sectionPath = fileSystem.getPath(NOTE_SECTION_FILE_NAME);
        if (Files.exists(sectionPath)) Files.delete(sectionPath);

        try (OutputStream os = Files.newOutputStream(sectionPath)) {
            try {
                marshaller.marshal(this, os);
            } catch (JAXBException e) {
                throw new IOException("unable to marshall note section",e);
            }
        }
    }

    private void saveNote(Note note) throws IOException {
        if (note==null) throw new IllegalArgumentException("note must not be null");
        NoteRef noteRef = getNoteRefByUUID(note.getUUID());
        noteRef.setName(note.getName());
        long modified = System.currentTimeMillis();
        note.setModified(modified);
        setModified(modified);
        logger.debug("saving note UUID=" + note.getUUID());
        final Marshaller marshaller=createMarshaller();
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(),true)) {

            saveSection(fileSystem,marshaller);

            // saving the note
            // ****************
            Path notePath = fileSystem.getPath(note.getUUID()+".xml");
            if (Files.exists(notePath)) Files.delete(notePath);
            try (OutputStream os = Files.newOutputStream(notePath)) {
                try {
                    marshaller.marshal(note, os);
                } catch (JAXBException e) {
                    throw new IOException("unable to marshall note",e);
                }
            }
        }
    }

    private void delNoteFromZip(NoteRef noteRef) throws IOException {
        long modified = System.currentTimeMillis();
        getNoteReferences().remove(noteRef);
        setModified(modified);
        final Marshaller marshaller=createMarshaller();
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(),true)) {

            saveSection(fileSystem,marshaller);

            // deleting the note
            // ****************
            Path notePath = fileSystem.getPath(UUID+".xml");
            if (Files.exists(notePath)) Files.delete(notePath);
        }
    }

    public synchronized void changeNoteName(String noteUUID, String newName) throws IOException {
        NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);
        Note note = readNote(noteRefByUUID.getUUID());
        note.setName(newName);
        saveNote(note);
        setChanged();
        notifyObservers(new NoteRenamed(getUUID(), noteUUID, newName));
    }

    private boolean nameExist(String name) {
        for (NoteRef noteRef:getNoteReferences()) {
            if (noteRef.getName().equals(name)) return true;
        }
        return false;
    }

    void addNote(Note note) throws IOException{
        NoteRef ref = new NoteRef(note.getUUID(),note.getName());
        getNoteReferences().add(ref);
        saveNote(note);
        setChanged();
        notifyObservers(new NoteAdded(note.getUUID(), note.getName()));
    }

    public synchronized void addNote() throws IOException {
        final Note note = new Note();
        String newName = "NEW NOTE";
        String name =newName;
        int i=0;
        while (nameExist(name)) {
            i++;
            name=newName+" "+i;
        }
        note.setName(name);
        addNote(note);
    }

    /**
     * remove a note from current section
     * @param UUID : the uuid of the note to delete
     */
    public synchronized void delNote(String UUID) throws IOException {
        NoteRef note = getNoteRefByUUID(UUID);
        delNoteFromZip(note);
        setChanged();
        notifyObservers(new NoteDeleted(note.getUUID()));
    }

    public void delete() throws IOException {
        logger.debug("deleting section "+getName());
        Files.delete(getPath());
    }

    public synchronized void saveNoteContent(String noteUUID,String content) throws IOException {
        NoteRef noteRef = getNoteRefByUUID(noteUUID);
        Note note = readNote(noteRef.getUUID());
        note.setContent(content);
        saveNote(note);
        setChanged();
        notifyObservers(new NoteContentChanged(getUUID(),noteUUID,content));
    }

    private long   modified ;
    private long   created  ;
    private List<NoteRef> notes = new ArrayList<>();
    private String UUID     ;
    private Path   path     ;

    private static final JAXBContext jaxbContext ;
    private static final Logger logger = Logger.getLogger(NoteSection.class);

    static {
        try {
            jaxbContext = JAXBContext.newInstance(NoteSection.class,Note.class);
        } catch (JAXBException e) {
            throw new RuntimeException("initialization error",e);
        }
    }


    private static final String NOTE_SECTION_FILE_NAME = "notesection.xml";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteSection that = (NoteSection) o;

        if (modified != that.modified) return false;
        if (created != that.created) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        if (UUID != null ? !UUID.equals(that.UUID) : that.UUID != null) return false;
        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (modified ^ (modified >>> 32));
        result = 31 * result + (int) (created ^ (created >>> 32));
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (UUID != null ? UUID.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    public String getNoteContent(String UUID) throws IOException {
        Note note = readNote(UUID);
        return note.getContent();
    }
}
