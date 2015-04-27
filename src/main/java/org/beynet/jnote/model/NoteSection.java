package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

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
    public void changeName(String noteBookName,String newName,IndexWriter writer) throws IOException {
        Path newPath = path.resolveSibling(newName + ".zip");
        Files.move(path, newPath);
        path=newPath;
        reIndexAllNotes(noteBookName,writer);
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

    NoteRef getNoteRefByUUID(String UUID) throws IllegalArgumentException {
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
     * @param UUID : the note UUID
     * @return : the note found (will nether be null)
     * @throws IOException
     * @throws IllegalArgumentException
     */
    protected Note readNote(String UUID) throws IOException,IllegalArgumentException {
        //check if the note exists
        getNoteRefByUUID(UUID);

        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(),false)) {
            return _readNote(UUID,fileSystem);
        }
    }

    private Note _readNote(String UUID,FileSystem fileSystem) throws IOException,IllegalArgumentException {
        //check if the note exists
        getNoteRefByUUID(UUID);

        Path path = fileSystem.getPath(UUID + ".xml");
        InputSource source = new InputSource();
        source.setEncoding("UTF-8");
        source.setByteStream(Files.newInputStream(path));
        Note note = null;
        try {
            note = (Note)jaxbContext.createUnmarshaller().unmarshal(source);
        } catch (JAXBException e) {
            throw new IOException("unable to read note",e);
        }
        return note;
    }

    /**
     * add an attachment to a note
     * @param noteUUID : the note UUID
     * @param file     : the file to attach
     * @throws IOException
     * @throws AttachmentAlreadyExistException
     */
    public synchronized void addNoteAttachment(String noteUUID,Path file) throws IOException, AttachmentAlreadyExistException {
        _addNoteAttachment(noteUUID,file.getFileName().toString(),Files.size(file),false,(destination)->{
            try {
                Files.copy(file,destination);
                return null;
            } catch (IOException e) {
                return e;
            }
        });
    }

    /**
     * add an attachment to a note
     * @param noteUUID : the note UUID
     * @param fileName
     * @param fileContent
     */
    public synchronized void addNoteAttachment(String noteUUID,String fileName,byte[] fileContent,boolean override) throws IOException, AttachmentAlreadyExistException {
        int length = fileContent.length;
        _addNoteAttachment(noteUUID,fileName,length,override,(destination)->{
            try (InputStream is = new ByteArrayInputStream(fileContent)){
                Files.copy(is,destination);
                return null;
            } catch (IOException e) {
                return e;
            }
        });
    }

    private void _addNoteAttachment(String noteUUID,String fileName,long length,boolean override,Function<Path,IOException> doTheCopy) throws IOException, AttachmentAlreadyExistException {
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), true)) {
            NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);
            Path path = fileSystem.getPath(noteUUID + "_" + fileName);
            if (Files.exists(path)) {
                if (override==false) throw new AttachmentAlreadyExistException();
                Files.delete(path);
            }
            Note note = _readNote(noteUUID, fileSystem);
            Attachment attachment = new Attachment();
            attachment.setName(fileName);
            attachment.setSize(length);
            note.getAttachments().add(attachment);
            _saveNote(note, fileSystem);
            IOException result = doTheCopy.apply(path);
            if (result!=null) throw result;
            noteRefByUUID.addAttachment(attachment);
        }
    }

    /**
     * @param noteUUID
     * @param fileName
     * @return
     * @throws AttachmentNotFoundException
     * @throws IOException
     */
    public synchronized byte[] readNoteAttachment(String noteUUID,String fileName) throws AttachmentNotFoundException, IOException {
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), false)) {
            getNoteRefByUUID(noteUUID);
            Path path = fileSystem.getPath(noteUUID + "_" + fileName);
            if (!Files.exists(path)) {
                throw new AttachmentNotFoundException();
            }
            return Files.readAllBytes(path);
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
        logger.debug("saving note UUID=" + note.getUUID());
        if (note==null) throw new IllegalArgumentException("note must not be null");

        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), true)) {
            _saveNote(note,fileSystem);
        }
    }

    private void _saveNote(Note note,FileSystem fileSystem) throws IOException {
        if (note==null) throw new IllegalArgumentException("note must not be null");
        if (fileSystem==null) throw new IllegalArgumentException("fileSystem must not be null");
        final Marshaller marshaller=createMarshaller();
        NoteRef noteRef = getNoteRefByUUID(note.getUUID());
        noteRef.setName(note.getName());
        long modified = System.currentTimeMillis();
        note.setModified(modified);
        setModified(modified);

        saveSection(fileSystem, marshaller);

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


    private void delNoteFromZip(NoteRef noteRef) throws IOException {
        long modified = System.currentTimeMillis();
        for (NoteRef n : getNoteReferences()) {
            if (n.getUUID().equals(noteRef.getUUID())) {
                getNoteReferences().remove(n);
                break;
            }
        }
        setModified(modified);
        final Marshaller marshaller=createMarshaller();
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), true)) {

            saveSection(fileSystem,marshaller);

            // deleting the note
            // ****************
            Path notePath = fileSystem.getPath(noteRef.getUUID()+".xml");
            if (Files.exists(notePath)) Files.delete(notePath);
        }
    }

    /**
     * change the name of the given note
     * @param noteUUID
     * @param newName
     * @throws IOException
     */
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

    /**
     * create a new empty note and add it to current section
     * @throws IOException
     */
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
     * @param noteUUID
     * @throws IOException
     */
    public synchronized void delNote(String noteUUID) throws IOException {
        NoteRef note = getNoteRefByUUID(noteUUID);
        delNoteFromZip(note);
        setChanged();
        notifyObservers(new NoteDeleted(note.getUUID()));
    }

    public void delete() throws IOException {
        logger.debug("deleting section " + getName());
        Files.delete(getPath());
    }

    /**
     * save the updated content of a note
     * @param noteUUID
     * @param content
     * @param writer
     * @throws IOException
     */
    public synchronized void saveNoteContent(String noteBookName,String noteUUID, String content, IndexWriter writer) throws IOException {
        NoteRef noteRef = getNoteRefByUUID(noteUUID);
        Note note = readNote(noteRef.getUUID());
        note.setContent(content);
        saveNote(note);
        setChanged();
        notifyObservers(new NoteContentChanged(getUUID(), noteUUID, content));
        indexNote(noteBookName,note,writer);
    }

    private void indexNote(String noteBookName,Note note,IndexWriter writer) throws IOException {

        StringField sectionUUID = new StringField(LuceneConstants.SECTION_UUID,getUUID(), Field.Store.YES);
        TextField  sectionName = new TextField(LuceneConstants.SECTION_NAME,getName(), Field.Store.YES);
        StringField noteBookNameField = new StringField(LuceneConstants.NOTE_BOOK_NAME,noteBookName, Field.Store.YES);

        Document document = new Document();
        document.add(sectionUUID);
        document.add(sectionName);
        document.add(noteBookNameField);

        Term uuidTerm = new Term(LuceneConstants.NOTE_UUID,note.getUUID());
        Query query = new TermQuery(uuidTerm);
        writer.deleteDocuments(query);
        String noteContent = note.getContent();
        if (noteContent==null) noteContent="";
        StringBuilder content = new StringBuilder(noteContent);
        content.append(" ");
        content.append(note.getName());
        content.append(" ");
        content.append(document.getField(LuceneConstants.SECTION_NAME));
        content.append(" ");
        content.append(document.getField(LuceneConstants.NOTE_BOOK_NAME));


        StringField noteUUID = new StringField(LuceneConstants.NOTE_UUID,note.getUUID(), Field.Store.YES);
        TextField noteContentField = new TextField(LuceneConstants.NOTE_CONTENT,content.toString(), Field.Store.YES);
        TextField noteName = new TextField(LuceneConstants.NOTE_NAME,note.getName(), Field.Store.YES);


        document.add(noteUUID);
        document.add(noteContentField);
        document.add(noteName);
        writer.addDocument(document);
        writer.commit();
    }



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

    /**
     * @param UUID : the requested note UUID
     * @return the content of the requested note
     * @throws IOException
     */
    public synchronized String getNoteContent(String UUID) throws IOException {
        Note note = readNote(UUID);
        return note.getContent();
    }

    /**
     * start to observe requested note
     * @param noteUUID : the requested note UUID
     * @param observer
     */
    public synchronized void subscribeToNote(String noteUUID, Observer observer) {
        NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);
        try {
            Note note = readNote(noteUUID);
            noteRefByUUID.addObserver(observer,note.getAttachments());
        } catch (IOException e) {
            logger.error("unable to read note",e);
        }
    }

    /**
     * stop observing requested note
     * @param noteUUID : the requested note
     * @param observer
     */
    public synchronized void unSubscribeToNote(String noteUUID, Observer observer) {
        NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);
        noteRefByUUID.deleteObserver(observer);
    }

    /**
     * remove an attachment from note
     * @param attachmentRef
     * @throws IOException
     * @throws AttachmentNotFoundException
     */
    public synchronized void deleteAttachment(AttachmentRef attachmentRef) throws IOException, AttachmentNotFoundException {
        logger.debug("delete attachment name=" + attachmentRef.getFileName() + " from note :" + attachmentRef.getNoteRef().getUUID());
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), true)) {
            String noteUUID = attachmentRef.getNoteRef().getUUID();
            NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);

            Path path = fileSystem.getPath(noteUUID + "_" + attachmentRef.getFileName());
            if (!Files.exists(path)) {
                throw new AttachmentNotFoundException();
            }
            Note note = _readNote(noteUUID, fileSystem);
            Attachment removed = null;
            for (Attachment attachment:note.getAttachments()) {
                if (attachment.getName().equals(attachmentRef.getFileName())) {
                    note.getAttachments().remove(attachment);
                    removed = attachment;
                    break;
                }
            }
            _saveNote(note, fileSystem);
            Files.delete(path);
            noteRefByUUID.removeAttachment(removed);
        }
    }

    public synchronized void saveAttachment(AttachmentRef attachmentRef, Path destination) throws IOException, AttachmentNotFoundException {
        logger.debug("save attachment name=" + attachmentRef.getFileName() + " from note :" + attachmentRef.getNoteRef().getUUID());
        try (FileSystem fileSystem = NoteSection.getZipFileSystem(this.getPath(), false)) {
            String noteUUID = attachmentRef.getNoteRef().getUUID();
            NoteRef noteRefByUUID = getNoteRefByUUID(noteUUID);

            Path path = fileSystem.getPath(noteUUID + "_" + attachmentRef.getFileName());
            if (!Files.exists(path)) {
                throw new AttachmentNotFoundException();
            }
            Files.copy(path,destination.resolve(attachmentRef.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     *
     * @param noteBookName
     * @param writer
     * @throws IOException
     */
    public synchronized void reIndexAllNotes(String noteBookName,IndexWriter writer) throws IOException {
        for (NoteRef noteRef:notes) {
            Note note = readNote(noteRef.getUUID());
            indexNote(noteBookName,note,writer);
        }
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

}
