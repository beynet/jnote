package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.model.events.NoteAdded;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A note section is a list of notes
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
        for (Note note : getNotes()) {
            o.update(this,new NoteAdded(note.getUUID(),note.getName(),note.getContent()));
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
    List<Note> getNotes(){
        return notes;
    }


    @XmlAttribute(name="modified")
    public long getModified() {
        return modified;
    }
    public void setModified(long modified) {
        this.modified = modified;
    }

    /**
     * @return current notesection marshalled in a string
     */
    @XmlTransient
    public String getXMLContentAsString() {
        StringWriter writer = new StringWriter();
        try {
            jaxbContext.createMarshaller().marshal(this, writer);
        } catch (JAXBException e) {
            throw new RuntimeException("unable to save note section",e);
        }
        return writer.toString();
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

    public void save() throws IOException {
        final URI uri = URI.create("jar:" + path.toUri().toString());
        System.out.println(uri);
        final Map<String, String> env = new HashMap<>();
        if (!Files.exists(path)) {
            env.put("create", "true");
        }
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            Path path = fileSystem.getPath("notesection.xml");
            if (Files.exists(path)) Files.delete(path);
            final Marshaller marshaller = jaxbContext.createMarshaller();
            try (OutputStream os = Files.newOutputStream(path)) {
                marshaller.marshal(this, os);
            }
        } catch(JAXBException e) {
            throw new IOException("unable to marshall note section",e);
        }

    }

    /**
     * change current section name
     * @param newName
     * @throws IOException
     */
    public void changeName(String newName) throws IOException {
        Path newPath = path.getParent().resolve(newName+".zip");
        Files.move(path,newPath);
        path=newPath;
    }


    public static NoteSection fromZipFile(Path zipFile) throws IllegalArgumentException {
        NoteSection result = new NoteSection();
        final Note note = new Note();
        note.setName("default");
        result.getNotes().add(note);
        if (Files.exists(zipFile)) {
            final URI uri = URI.create("jar:" + zipFile.toUri().toString());
            final Map<String, String> env = new HashMap<>();
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
                Path path = fileSystem.getPath("notesection.xml");
                if (!Files.exists(path)) throw new IllegalArgumentException("zip does not contain expected file");
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
        result.path = zipFile;
        return result;
    }

    @XmlTransient
    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteSection that = (NoteSection) o;

        if (modified != that.modified) return false;
        if (created != that.created) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        return !(UUID != null ? !UUID.equals(that.UUID) : that.UUID != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (modified ^ (modified >>> 32));
        result = 31 * result + (int) (created ^ (created >>> 32));
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (UUID != null ? UUID.hashCode() : 0);
        return result;
    }

    @XmlTransient
    public String getName() {
        String fileName = path.getFileName().toString();
        int i = fileName.indexOf(".");
        if (i==-1) return fileName;
        else return fileName.substring(0,i);
    }

    private long   modified ;
    private long   created  ;
    private List<Note> notes = new ArrayList<>();
    private String UUID     ;
    private Path   path     ;

    private static final JAXBContext jaxbContext ;
    private static final Logger logger = Logger.getLogger(NoteSection.class);

    static {
        try {
            jaxbContext = JAXBContext.newInstance(NoteSection.class);
        } catch (JAXBException e) {
            throw new RuntimeException("initialization error",e);
        }
    }
}
