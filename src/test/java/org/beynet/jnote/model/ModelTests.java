package org.beynet.jnote.model;

import org.beynet.jnote.DefaultTest;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by beynet on 06/04/2015.
 */
public class ModelTests extends DefaultTest {
    @Test
    public void saveNote() throws IOException {
        String htmlContent = "<html></html>";

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path testFile = Files.createTempFile(tmpDir, "notesectiontest", ".zip");
        if (Files.exists(testFile)) Files.delete(testFile);

        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(testFile);
        Note note = new Note();
        note.setContent(htmlContent);
        noteSection.addNote(note);


        NoteSection noteSection2 = NoteSection.fromAbsoluteZipFilePath(testFile);
        assertThat(noteSection2, is(noteSection));

        Note note2 = noteSection2.readNote(note.getUUID());
        assertThat(note2, is(note));
    }

    @Test(expected = AttachmentAlreadyExistException.class)
    public void attachmentAlreadyExist() throws IOException, AttachmentAlreadyExistException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path testFile = Files.createTempFile(tmpDir, "notesectiontest", ".zip");
        if (Files.exists(testFile)) Files.delete(testFile);
        String htmlContent = "<html></html>";
        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(testFile);
        Note note = new Note();
        note.setContent(htmlContent);
        noteSection.addNote(note);

        noteSection.addNoteAttachment(note.getUUID(), "truc.txt", Files.readAllBytes(Paths.get("/etc/passwd")), false);
        noteSection.addNoteAttachment(note.getUUID(), "truc.txt", Files.readAllBytes(Paths.get("/etc/passwd")), false);
    }

    @Test
    public void saveAndReadAttachment() throws IOException, AttachmentAlreadyExistException, AttachmentNotFoundException, NoSuchAlgorithmException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path testFile = Files.createTempFile(tmpDir, "notesectiontest", ".zip");
        if (Files.exists(testFile)) Files.delete(testFile);
        String htmlContent = "<html></html>";
        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(testFile);
        Note note = new Note();
        note.setName("note width attachment");
        note.setContent(htmlContent);
        noteSection.addNote(note);

        String fileName = "truc.txt";
        byte[] expected = Files.readAllBytes(Paths.get("/etc/passwd"));
        noteSection.addNoteAttachment(note.getUUID(), fileName, expected, false);
        byte[] bytes = noteSection.readNoteAttachment(note.getUUID(), fileName);

        assertThat(expected, is(bytes));
    }

    /**
     * in this test we create "by hand" a note book with one section
     * and we check if we load it correctly using model class
     *
     * @throws IOException
     */
    @Test
    public void load() throws IOException {
        Model model = null;
        try {
            final String name = "section1";
            Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir, ".jnote"));
            Path nb1 = root.resolve("nb1");
            Files.createDirectories(nb1);
            NoteBook nb = new NoteBook(nb1);
            NoteSection section1 = nb.createNewEmptySection(name);
            assertThat(section1.getName(), is(name));
            Note note1 = new Note();
            note1.setContent("<html><head><title>this is the title</title></head></html>");
            section1.addNote(note1);

            model = new Model(root);

            Map<String, NoteBook> noteBooks = model.noteBooks;
            assertThat(Integer.valueOf(noteBooks.size()), is(Integer.valueOf(1)));


            Map<String, NoteSection> sections = noteBooks.values().iterator().next().sectionsMap;
            assertThat(Integer.valueOf(sections.size()), is(Integer.valueOf(1)));
            NoteSection sectionFound = sections.values().iterator().next();
            assertThat(sectionFound, is(section1));
            assertThat(sectionFound.readNote(note1.getUUID()), is(note1));
        } finally {
            if (model!=null) model.delete();
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void createSectionWithNameAlreadyExisting() throws IOException {
        final String name = "section1";
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir, ".jnote"));
        Path nb1 = root.resolve("nb1");
        Files.createDirectories(nb1);
        NoteBook nb = new NoteBook(nb1);
        nb.createNewEmptySection(name);
        nb.createNewEmptySection(name);
    }

    @Test
    public void moveNoteWithModel() throws IOException, AttachmentAlreadyExistException {

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path fileToAttach = Files.createTempFile(tmpDir, "test_", ".txt");
        Files.write(fileToAttach,"123456789".getBytes("UTF-8"), StandardOpenOption.APPEND,StandardOpenOption.CREATE);

        Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir, ".jnote"));
        Model model=null;
        try {
            model = new Model(root);
            //create two books
            model.addNoteBook("book1");
            model.addNoteBook("book2");
            NoteBook book1=null,book2=null;
            for (Map.Entry<String, NoteBook> entry : model.noteBooks.entrySet()) {
                if ("book1".equals(entry.getValue().getName())) {
                    book1 = entry.getValue();
                }
                if ("book2".equals(entry.getValue().getName())) {
                    book2 = entry.getValue();
                }
            }
            assertThat(book1,is(notNullValue()));
            assertThat(book2,is(notNullValue()));
            // create a section in each book
            model.createNewSection(book1.getUUID());
            model.createNewSection(book2.getUUID());

            NoteSection book1Section1=null,book2Section1=null;
            book1Section1=book1.sectionsMap.values().iterator().next();
            book2Section1=book2.sectionsMap.values().iterator().next();

            // change the section name of each book
            model.changeSectionName(book1.getUUID(),book1Section1.getUUID(),"book1 section1");
            model.changeSectionName(book2.getUUID(),book2Section1.getUUID(),"book2 section1");


            //search the default note created in each section
            NoteRef book1Section1Note1=null,book2Section1Note1=null;
            book1Section1Note1=book1Section1.getNoteReferences().get(0);
            book2Section1Note1=book2Section1.getNoteReferences().get(0);

            model.saveNoteContent(book1.getUUID(),book1Section1.getUUID(),book1Section1Note1.getUUID(),"<html><head></head><body><p>note1 book1</body></html>");
            model.saveNoteContent(book2.getUUID(),book2Section1.getUUID(),book2Section1Note1.getUUID(),"<html><head></head><body><p>note1 book2../..</body></html>");

            // add an attachment in the note that will be move - check that the file has been attached correctly
            book1Section1.addNoteAttachment(book1Section1Note1.getUUID(),fileToAttach);
            Note note = book1Section1.readNote(book1Section1Note1.getUUID());
            assertThat(note.getAttachments().size(),is(1));
            assertThat(note.getAttachments().get(0).getName(),is(fileToAttach.getFileName().toString()));
            assertThat(note.getAttachments().get(0).getSize(),is(Files.size(fileToAttach)));


            // move the note from book1 section 1 to book2 section1
            model.moveNote(book1.getUUID(),book1Section1.getUUID(),note.getUUID(),book2.getUUID(),book2Section1.getUUID());

            NoteRef noteFound = null;
            for (NoteRef noteRef : book2Section1.getNoteReferences()) {
                if (book1Section1Note1.getUUID().equals(noteRef.getUUID())) {
                    noteFound=noteRef;
                    break;
                }
            }
            assertThat(noteFound,is(notNullValue()));
            note = book2Section1.readNote(noteFound.getUUID());
            assertThat(note.getAttachments().size(),is(1));
            assertThat(note.getAttachments().get(0).getName(),is(fileToAttach.getFileName().toString()));
            assertThat(note.getAttachments().get(0).getSize(),is(Files.size(fileToAttach)));


            //book1Section1.set
            //model.createNewSection();
        }finally {
            if (model!=null) model.delete();
        }

    }

}
