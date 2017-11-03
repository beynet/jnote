package org.beynet.jnote.controler;

import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.beynet.jnote.model.Model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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

    public static void subscribeToNote(NoteRef noteRef, Observer observer) {
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        Model.getInstance().subscribeToNote(noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID(),observer);
    }
    public static void unSubscribeToNote(NoteRef noteRef, Observer observer) {
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        Model.getInstance().unSubscribeToNote(noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID(), observer);
    }


    public static void createNewSection(NoteBookRef noteBook) throws IOException {
        Model.getInstance().createNewSection(noteBook.getUUID());
    }

    public static void saveNoteContent(NoteBookRef noteBook, String sectionUUID, String noteUUID, String content) throws IOException{
        Model.getInstance().saveNoteContent(noteBook.getUUID(), sectionUUID, noteUUID, content);
    }

    public static void undoNoteContent(NoteBookRef noteBook, String sectionUUID, String noteUUID, String htmlText) throws IOException{
        Model.getInstance().undoNoteContent(noteBook.getUUID(), sectionUUID, noteUUID,htmlText);
    }


    public static void changeSectionName(NoteBookRef noteBook, String sectionUUID, String name) throws IOException {
        Model.getInstance().changeSectionName(noteBook.getUUID(), sectionUUID, name);
    }

    public static void onExit() {
        Model.getInstance().onExit();
    }


    public static void changeNoteName(NoteSectionRef noteSectionRef, String noteUUID, String text)  throws IOException {
        NoteBookRef noteBookRef = noteSectionRef.getNoteBookRef();
        Model.getInstance().changeNoteName(noteBookRef.getUUID(),noteSectionRef.getUUID(),noteUUID,text);
    }

    public static void addNote(NoteSectionRef noteSectionRef) throws IOException {
        NoteBookRef noteBookRef = noteSectionRef.getNoteBookRef();
        Model.getInstance().addNote(noteBookRef.getUUID(),noteSectionRef.getUUID());
    }

    public static void addNoteBook(String name) throws IOException{
        Model.getInstance().addNoteBook(name);
    }

    public static void delNote(NoteRef noteRef) throws IOException {
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        Model.getInstance().delNote(sectionRef.getNoteBookRef().getUUID(),sectionRef.getUUID(),noteRef.getUUID());
    }

    public static void delNoteBook(String noteBookUUID) throws IOException {
        Model.getInstance().delNoteBook(noteBookUUID);
    }

    public static String getNoteContent(NoteRef noteRef) throws IOException {
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        return Model.getInstance().getNoteContent(noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID());
    }

    public static void addAttachment(NoteRef noteRef, Path path) throws IOException, AttachmentAlreadyExistException {
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        Model.getInstance().addAttachment(noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID(),path);
    }

    public static void deleteAttachment(AttachmentRef attachmentRef) throws IOException, AttachmentNotFoundException {
        NoteRef noteRef = attachmentRef.getNoteRef();
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        Model.getInstance().deleteAttachment(attachmentRef.getFileName(),noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID());
    }

    public static void saveAttachment(AttachmentRef attachmentRef, Path path) throws IOException, AttachmentNotFoundException {
        NoteRef noteRef = attachmentRef.getNoteRef();
        NoteSectionRef sectionRef=noteRef.getNoteSectionRef();
        NoteBookRef noteBookRef = sectionRef.getNoteBookRef();
        Model.getInstance().saveAttachment(attachmentRef.getFileName(),noteBookRef.getUUID(),sectionRef.getUUID(),noteRef.getUUID(),path);
    }

    public static List<NoteRef> getMatchingNotes(String query) throws IOException {
        List<NoteRef> result = (List<NoteRef>)Model.getInstance().getMatchingNotes(query, (b,bn,s,sn,n,nn)-> {
                NoteBookRef noteBookRef = new NoteBookRef(b,bn);
                NoteSectionRef sectionRef = new NoteSectionRef(noteBookRef,s,sn);
                return new NoteRef(sectionRef,n,nn);
            }
        );
        return result;
    }

    public static void reIndexAllNotes() throws IOException {
        Model.getInstance().reIndexAllNotes();
    }

    public static void renameNoteBook(String currentUUID, String name) throws IOException {
        Model.getInstance().renameNoteBook(currentUUID, name);
    }

    public static void deleteSection(NoteSectionRef ref) throws IOException {
        NoteBookRef noteBookRef = ref.getNoteBookRef();
        Model.getInstance().deleteSection(noteBookRef.getUUID(),ref.getUUID());
    }
}
