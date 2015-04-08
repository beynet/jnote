package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.model.events.NewNoteBookEvent;
import org.beynet.jnote.model.events.OnExitEvent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by beynet on 06/04/2015.
 */
public class Model extends Observable implements FileVisitor<Path> {
    private static Model _instance = null;

    Model(Path rootDir) {
        this.rootDir = rootDir;
        loadNoteBooks();
    }

    public static Model createInstance(Path rootDir) {
        if (!Files.exists(rootDir)) {
            try {
                Files.createDirectories(rootDir);
            } catch (IOException e) {
                throw new IllegalArgumentException("unable to create target model directory",e);
            }
        }
        if (_instance==null) {
            _instance = new Model(rootDir);
            return _instance;
        }
        else {
            throw new IllegalArgumentException("instance already created");
        }
    }

    public static Model getInstance() {
        return _instance;
    }

    private void loadNoteBooks() {
        noteBooks=new HashMap<>();
        depth=-1;
        try {
            Files.walkFileTree(rootDir,this);
        } catch (IOException e) {
            logger.error("unable to load",e);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (depth<1) {
            depth++;
            if (depth==1) {
                currentNoteBook = new NoteBook(dir);
                noteBooks.put(currentNoteBook.getUUID(), currentNoteBook);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (depth==1) {
            if (file.getFileName().toString().endsWith(".zip")) {
                currentNoteBook.addSection(file.getFileName());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        depth--;
        return FileVisitResult.CONTINUE;
    }

    public void subscribeToNoteBook(String noteBookUUID,Observer observer) throws IllegalArgumentException {
        getNoteBookByUUID(noteBookUUID).addObserver(observer);
    }
    public void unSubscribeToNoteBook(String noteBookUUID,Observer observer) throws IllegalArgumentException {
        getNoteBookByUUID(noteBookUUID).deleteObserver(observer);
    }

    public void subscribeToNoteSection(String noteBookUUID, String sectionUUID,Observer observer) {
        getNoteBookByUUID(noteBookUUID).subscribeToNoteSection(sectionUUID, observer);
    }

    public void unSubscribeToNoteSection(String noteBookUUID, String sectionUUID,Observer observer) {
        getNoteBookByUUID(noteBookUUID).unSubscribeToNoteSection(sectionUUID, observer);
    }

    public void subscribe(Observer observer) {
        super.addObserver(observer);
        synchronized (noteBooks) {
            for (Map.Entry<String,NoteBook> entry : noteBooks.entrySet()) {
                observer.update(this, new NewNoteBookEvent(entry.getKey(),entry.getValue().getName()));
            }
        }
    }

    private NoteBook getNoteBookByUUID(String UUID) throws IllegalArgumentException {
        final NoteBook noteBook;
        synchronized (noteBooks) {
            noteBook = noteBooks.get(UUID);
        }
        if (noteBook==null) throw new IllegalArgumentException("invalid note book UUID");
        return noteBook;
    }

    public void createNewSection(String noteBookUUID) {
        getNoteBookByUUID(noteBookUUID).addSection();
    }
    public void saveSectionContent(String noteBookUUID, String sectionUUID, String noteUUID,String content) throws IOException{
        getNoteBookByUUID(noteBookUUID).saveSectionContent(sectionUUID, noteUUID, content);
    }

    public void changeSectionName(String noteBookUUID, String sectionUUID, String name) throws IOException{
        getNoteBookByUUID(noteBookUUID).changeSectionName(sectionUUID, name);
    }
    public void changeNoteName(NoteSectionRef noteSectionRef, String noteUUID, String text) throws IOException{
        getNoteBookByUUID(noteSectionRef.getNoteBookRef().getUUID()).changeNoteName(noteSectionRef.getUUID(),noteUUID,text);
    }

    public void onExit() {
        setChanged();
        notifyObservers(new OnExitEvent());
    }
    public void addNote(NoteSectionRef noteSectionRef) throws IOException {
        getNoteBookByUUID(noteSectionRef.getNoteBookRef().getUUID()).addNote(noteSectionRef.getUUID());
    }

    private Path rootDir ;
    private long depth ;
    Map<String,NoteBook> noteBooks ;
    private NoteBook currentNoteBook ;
    private final static Logger logger = Logger.getLogger(Model.class);


}
