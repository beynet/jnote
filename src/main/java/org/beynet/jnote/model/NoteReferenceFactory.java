package org.beynet.jnote.model;

public interface NoteReferenceFactory {
    Object constructFromUUIDsAndNames(String noteBookUUID, String noteBookName, String sectionUUID, String sectionName, String noteUUID, String noteName);
}
