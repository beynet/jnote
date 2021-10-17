package org.beynet.jnote.model;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beynet on 07/04/15.
 */
@XmlRootElement(name="Note")
@XmlType(name="NoteType")
public class Note  {

    public Note() {
        this.UUID = java.util.UUID.randomUUID().toString();
        this.modified=this.created=System.currentTimeMillis();
        this.name = "";
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

    @XmlAttribute(name="modified")
    public long getModified() {
        return modified;
    }
    public void setModified(long modified) {
        this.modified = modified;
    }

    @XmlElement(name="content")
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @XmlElement(name="previousContents")
    public List<String> getPreviousContent() {
        return previousContent;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name="attachments")
    @XmlElement(name="attachment")
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (modified != note.modified) return false;
        if (created != note.created) return false;
        if (content != null ? !content.equals(note.content) : note.content != null) return false;
        if (name != null ? !name.equals(note.name) : note.name != null) return false;
        return !(UUID != null ? !UUID.equals(note.UUID) : note.UUID != null);

    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (int) (modified ^ (modified >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (created ^ (created >>> 32));
        result = 31 * result + (UUID != null ? UUID.hashCode() : 0);
        return result;
    }




    private String content;
    private long   modified ;
    private String name;
    private long   created  ;
    private String UUID     ;
    private List<Attachment> attachments = new ArrayList<>();
    private List<String> previousContent = new ArrayList<>();

}
