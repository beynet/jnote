package org.beynet.jnote.controler;

import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.model.Model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Observer;

/**
 * Created by beynet on 06/04/2015.
 */
public class Controller {

    /**
     * subscribe to general model. Such a subscriber will receive :<ul>
     *     <li>notification when a new notebook is created</li>
     * </ul>
     *
     * @param observer
     */
    public static void subscribeToModel(Observer observer) {
        Model.getInstance().addObserver(observer);
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

    public static void createNewSection(NoteBookRef noteBook) throws IOException {
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

    public static void addNoteBook(String name) throws IOException{
        Model.getInstance().addNoteBook(name);
    }

    public static void delNote(NoteRef noteRef) throws IOException {
        Model.getInstance().delNote(noteRef);
    }

    public static void delNoteBook(String noteBookUUID) throws IOException {
        Model.getInstance().delNoteBook(noteBookUUID);
    }

    public static String getNoteContent(NoteRef noteRef) throws IOException {
        return Model.getInstance().getNoteContent(noteRef);
    }

    public static void addAttachment(NoteRef noteRef, Path path) throws IOException, AttachmentAlreadyExistException {
        Model.getInstance().addAttachment(noteRef, path);
    }
}
