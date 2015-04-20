package org.beynet.jnote.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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

    @XmlAttribute(name="mimeType")
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private String name ;
    private String mimeType ;

}
