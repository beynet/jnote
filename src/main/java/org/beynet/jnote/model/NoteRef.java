package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.model.events.note.AttachmentAddedToNote;
import org.beynet.jnote.model.events.note.AttachmentRemovedFromNote;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by beynet on 19/04/2015.
 */
@XmlType(name = "NoteRefType")
public class NoteRef  extends Observable {
    public NoteRef() {

    }
    public NoteRef(String UUID,String name) {
        this.UUID = UUID ;
        this.name = name ;
    }

    public synchronized void addObserver(Observer o,List<Attachment> attachments) {
        logger.debug("add observer to note UUID=" + getUUID());
        super.addObserver(o);
        for (Attachment attachment : attachments) {
            o.update(this,new AttachmentAddedToNote(getUUID(),attachment.getName(),attachment.getSize()));
        }
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        super.deleteObserver(o);
        logger.debug("delete observer from note UUID=" + getUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteRef noteRef = (NoteRef) o;

        if (name != null ? !name.equals(noteRef.name) : noteRef.name != null) return false;
        return !(UUID != null ? !UUID.equals(noteRef.UUID) : noteRef.UUID != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (UUID != null ? UUID.hashCode() : 0);
        return result;
    }

    public void addAttachment(Attachment attachment) {
        setChanged();
        notifyObservers(new AttachmentAddedToNote(getUUID(),attachment.getName(),attachment.getSize()));
    }

    public void removeAttachment(Attachment attachment) {
        setChanged();
        notifyObservers(new AttachmentRemovedFromNote(getUUID(),attachment.getName()));
    }
    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }
    protected void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="uuid")
    public String getUUID() {
        return UUID;
    }
    protected void setUUID(String UUID) {
        this.UUID = UUID;
    }

    private String name ;
    private String UUID ;

    private final static Logger logger = Logger.getLogger(Note.class);

}
