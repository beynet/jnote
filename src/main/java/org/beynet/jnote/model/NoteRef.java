package org.beynet.jnote.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by beynet on 19/04/2015.
 */
@XmlType(name = "NoteRefType")
public class NoteRef {

    public NoteRef(String UUID,String name) {
        this.UUID = UUID ;
        this.name = name ;
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

    public NoteRef() {

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

    String name ;
    String UUID ;
}
