package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by beynet on 06/04/2015.
 */
@XmlRootElement(name = "NoteSection")
public class NoteSection {
    NoteSection() {
        init();
    }

    public NoteSection(Path noteBookFile) {

    }

    private static void init() {
        this.UUID = java.util.UUID.randomUUID().toString();
        this.modified=this.created=System.currentTimeMillis();
        this.content=null;
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

    @XmlElement(name="content")
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
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

    public static Optional<NoteSection> fromUTF8ByteArray(byte[] utf8Array) {
        InputSource source = new InputSource();
        source.setEncoding("UTF-8");
        source.setByteStream(new ByteArrayInputStream(utf8Array));
        return fromInputSource(source);
    }

    /**
     * @param noteSection : the string buffer which contains the expected note section
     * @return note section constructed from provided string
     */
    public static Optional<NoteSection> fromString(String noteSection) {
        Optional<NoteSection> result = Optional.empty();
        InputSource source = new InputSource();
        source.setEncoding("UTF-8");
        try {
            source.setByteStream(new ByteArrayInputStream(noteSection.getBytes("UTF-8")));
            result = fromInputSource(source);
        }catch(UnsupportedEncodingException e) {
            logger.error("unable to unmarshal string content",e);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteSection that = (NoteSection) o;

        if (modified != that.modified) return false;
        if (created != that.created) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        return !(UUID != null ? !UUID.equals(that.UUID) : that.UUID != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (modified ^ (modified >>> 32));
        result = 31 * result + (int) (created ^ (created >>> 32));
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (UUID != null ? UUID.hashCode() : 0);
        return result;
    }

    private long   modified ;
    private long   created  ;
    private String content  ;
    private String UUID     ;

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
