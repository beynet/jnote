package org.beynet.jnote.controler;

import org.beynet.jnote.model.Model;

import java.io.IOException;
import java.util.Observer;

/**
 * Created by beynet on 06/04/2015.
 */
public class Controller {

    public static void subscribeToModel(Observer observer) {
        Model.getInstance().subscribe(observer);
    }

    public static void subscribeToNoteBook(NoteBookRef noteBook,Observer observer) {
        Model.getInstance().subscribeToNoteBook(noteBook.getUUID(), observer);
    }
    public static void unSubscribeToNoteBook(NoteBookRef noteBook,Observer observer) {
        Model.getInstance().unSubscribeToNoteBook(noteBook.getUUID(), observer);
    }

    public static void subscribeToNoteSection(NoteSectionRef noteSectionRef,Observer observer) {
        Model.getInstance().subscribeToNoteSection(noteSectionRef.getNoteBookRef().getUUID(), noteSectionRef.getUUID(), observer);
    }
    public static void unSubscribeToNoteSection(NoteSectionRef noteSectionRef,Observer observer) {
        Model.getInstance().unSubscribeToNoteSection(noteSectionRef.getNoteBookRef().getUUID(), noteSectionRef.getUUID(), observer);
    }

    public static void createNewSection(NoteBookRef noteBook) {
        Model.getInstance().createNewSection(noteBook.getUUID());
    }

    public static void saveNoteContent(NoteBookRef noteBook, String sectionUUID, String noteUUID, String content) throws IOException{
        Model.getInstance().saveNoteContent(noteBook.getUUID(), sectionUUID, noteUUID, content);
    }

    public static void changeSectionName(NoteBookRef noteBook, String sectionUUID, String name) throws IOException {
        Model.getInstance().changeSectionName(noteBook.getUUID(), sectionUUID, name);
    }

    public static void onExit() {
        Model.getInstance().onExit();
    }


    public static void changeNoteName(NoteSectionRef noteSectionRef, String noteUUID, String text)  throws IOException {
        Model.getInstance().changeNoteName(noteSectionRef, noteUUID, text);
    }

    public static void addNote(NoteSectionRef noteSectionRef) throws IOException {
        Model.getInstance().addNote(noteSectionRef);
    }
}
