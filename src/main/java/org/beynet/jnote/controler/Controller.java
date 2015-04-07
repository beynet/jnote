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
    public static void createNewSection(NoteBookRef noteBook) {
        Model.getInstance().createNewSection(noteBook.getUUID());
    }

    public static void saveSectionContent(NoteBookRef noteBook,String sectionUUID,String content) {
        Model.getInstance().saveSectionContent(noteBook.getUUID(), sectionUUID, content);
    }

    public static void changeSectionName(NoteBookRef noteBook, String sectionUUID, String name) throws IOException {
        Model.getInstance().changeSectionName(noteBook.getUUID(), sectionUUID, name);
    }

    public static void onExit() {
        Model.getInstance().onExit();
    }
}
