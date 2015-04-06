package org.beynet.jnote.model;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by beynet on 06/04/2015.
 */
public class Model implements FileVisitor<Path>{
    public Model(Path rootDir) {
        this.rootDir = rootDir;
        loadNoteBooks();
    }

    private void loadNoteBooks() {
        noteBooks=new ArrayList<>();
        depth=-1;
        try {
            Files.walkFileTree(rootDir,this);
        } catch (IOException e) {
            logger.error("unable to load",e);
        }
    }

    private Path rootDir ;
    private long depth ;
    private List<NoteBook> noteBooks = new ArrayList<>();
    private NoteBook currentNoteBook ;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (depth<1) {
            depth++;
            if (depth==1) {
                currentNoteBook = new NoteBook(dir.getFileName().toString());
                noteBooks.add(currentNoteBook);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (depth==1) {
            if (file.getFileName().toString().endsWith(".zip")) {
                currentNoteBook.addNoteSection(file);
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


    private final static Logger logger = Logger.getLogger(Model.class);
}
