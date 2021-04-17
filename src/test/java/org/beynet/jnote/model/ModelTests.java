package org.beynet.jnote.model;

import org.beynet.jnote.DefaultTest;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(noteSection,noteSection2);

        Note note2 = noteSection2.readNote(note.getUUID());
        assertEquals(note, note2);
    }

    //@Test(expected = AttachmentAlreadyExistException.class)
    @Test
    public void attachmentAlreadyExist() throws IOException, AttachmentAlreadyExistException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path sectionFile = Files.createTempFile(tmpDir, "notesectiontest", ".zip");
        Path fileToAttach = Files.createTempFile(tmpDir,"tmpFile",".dat");
        try {
            if (Files.exists(sectionFile)) Files.delete(sectionFile);
            if (Files.exists(fileToAttach)) Files.delete(fileToAttach);
            Files.write(fileToAttach, "this is the file content. to be written\n\r".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            String htmlContent = "<html></html>";
            NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(sectionFile);
            Note note = new Note();
            note.setContent(htmlContent);
            noteSection.addNote(note);

            noteSection.addNoteAttachment(note.getUUID(), "truc.txt", Files.readAllBytes(fileToAttach), false);
            assertThrows(AttachmentAlreadyExistException.class, () -> {
                noteSection.addNoteAttachment(note.getUUID(), "truc.txt", Files.readAllBytes(fileToAttach), false);
            });
        }
        finally {
            if (Files.exists(sectionFile)) try {
                Files.delete(sectionFile);
            }catch(IOException e) {

            }
            if (Files.exists(fileToAttach)) try {
                Files.delete(fileToAttach);
            } catch(IOException e) {

            }
        }
    }

    @Test
    public void saveAndReadAttachment() throws IOException, AttachmentAlreadyExistException, AttachmentNotFoundException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path sectionFile = Files.createTempFile(tmpDir, "notesectiontest", ".zip");
        Path fileToAttach = Files.createTempFile(tmpDir,"tmpFile",".dat");
        try {
            if (Files.exists(sectionFile)) Files.delete(sectionFile);
            Files.write(fileToAttach,"this is the file content. to be written\n\r".getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
            String htmlContent = "<html></html>";
            NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(sectionFile);
            Note note = new Note();
            note.setName("note width attachment");
            note.setContent(htmlContent);
            noteSection.addNote(note);

            String fileName = "truc.txt";
            byte[] expected = Files.readAllBytes(fileToAttach);
            noteSection.addNoteAttachment(note.getUUID(), fileName, expected, false);
            byte[] bytes = noteSection.readNoteAttachment(note.getUUID(), fileName);
            assertTrue(Arrays.equals(expected,bytes));
        } finally {
            if (Files.exists(sectionFile)) try {
                Files.delete(sectionFile);
            }catch(IOException e) {

            }
            if (Files.exists(fileToAttach)) try {
                Files.delete(fileToAttach);
            } catch(IOException e) {

            }
        }
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
            assertEquals(name,section1.getName());
            Note note1 = new Note();
            note1.setContent("<html><head><title>this is the title</title></head></html>");
            section1.addNote(note1);

            model = new Model(root);

            Map<String, NoteBook> noteBooks = model.noteBooks;
            assertEquals(Integer.valueOf(1),Integer.valueOf(noteBooks.size()));


            Map<String, NoteSection> sections = noteBooks.values().iterator().next().sectionsMap;
            assertEquals(Integer.valueOf(1),Integer.valueOf(sections.size()));
            NoteSection sectionFound = sections.values().iterator().next();
            assertEquals(section1,sectionFound);
            assertEquals(note1,sectionFound.readNote(note1.getUUID()));
        } finally {
            if (model!=null) model.delete();
        }

    }

    @Test
    public void createSectionWithNameAlreadyExisting() throws IOException {
        final String name = "section1";
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir, ".jnote"));
        Path nb1 = root.resolve("nb1");
        Files.createDirectories(nb1);
        NoteBook nb = new NoteBook(nb1);
        nb.createNewEmptySection(name);
        assertThrows(IllegalArgumentException.class,()->nb.createNewEmptySection(name));
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
            assertNotNull(book1);
            assertNotNull(book1);
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
            assertEquals(1,note.getAttachments().size());
            assertEquals(fileToAttach.getFileName().toString(),note.getAttachments().get(0).getName());
            assertEquals(Files.size(fileToAttach),note.getAttachments().get(0).getSize());


            // move the note from book1 section 1 to book2 section1
            model.moveNote(book1.getUUID(),book1Section1.getUUID(),note.getUUID(),book2.getUUID(),book2Section1.getUUID());

            NoteRef noteFound = null;
            for (NoteRef noteRef : book2Section1.getNoteReferences()) {
                if (book1Section1Note1.getUUID().equals(noteRef.getUUID())) {
                    noteFound=noteRef;
                    break;
                }
            }
            assertNotNull(noteFound);
            note = book2Section1.readNote(noteFound.getUUID());
            assertEquals(1,note.getAttachments().size());
            assertEquals(fileToAttach.getFileName().toString(),note.getAttachments().get(0).getName());
            assertEquals(Files.size(fileToAttach),note.getAttachments().get(0).getSize());


            //book1Section1.set
            //model.createNewSection();
        }finally {
            if (model!=null) model.delete();
        }

    }

}
