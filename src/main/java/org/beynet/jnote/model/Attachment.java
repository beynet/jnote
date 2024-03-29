package org.beynet.jnote.model;


import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Created by beynet on 20/04/2015.
 */

@XmlType(name = "AttachmentType")
public class Attachment {

    public Attachment() {

    }

    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="size")
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }

    @XmlAttribute(name="mimeType")
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private String name ;
    private String mimeType ;
    private long   size ;

}
