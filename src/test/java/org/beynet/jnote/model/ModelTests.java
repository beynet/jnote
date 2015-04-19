package org.beynet.jnote.model;

import org.beynet.jnote.DefaultTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by beynet on 06/04/2015.
 */
public class ModelTests extends DefaultTest{
    @Test
    public void saveNote() throws IOException {
        String htmlContent = "<html></html>";

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path testFile = Files.createTempFile(tmpDir,"notesectiontest",".zip");
        if (Files.exists(testFile)) Files.delete(testFile);

        NoteSection noteSection = NoteSection.fromAbsoluteZipFilePath(testFile);
        Note note = new Note();
        note.setContent(htmlContent);
        noteSection.addNote(note);


        NoteSection noteSection2 = NoteSection.fromAbsoluteZipFilePath(testFile);
        assertThat(noteSection2,is(noteSection));

        Note note2 = noteSection2.readNote(note.getUUID());
        assertThat(note2,is(note));
    }

    /**
     * in this test we create "by hand" a note book with one section
     * and we check if we load it correctly using model class
     * @throws IOException
     */
    @Test
    public void load() throws IOException {
        /*final String name = "section1";
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir,".jnote"));
        Path nb1 = root.resolve("nb1");
        Files.createDirectories(nb1);
        NoteBook nb = new NoteBook(nb1);
        NoteSection section1 = nb.addSection(name);
        assertThat(section1.getName(),is(name));
        section1.setContent("<html><head><title>this is the title</title></head></html>");
        section1.save();

        Model model = new Model(root);

        Map<String,NoteBook> noteBooks = model.noteBooks;
        assertThat(Integer.valueOf(noteBooks.size()), is(Integer.valueOf(1)));


        Map<String,NoteSection> sections = noteBooks.values().iterator().next().sectionsMap;
        assertThat(Integer.valueOf(sections.size()), is(Integer.valueOf(1)));
        NoteSection sectionFound = sections.values().iterator().next();

        assertThat(sectionFound,is(section1));*/

    }

    @Test(expected = IllegalArgumentException.class)
    public void createSectionWithNameAlreadyExisting() throws IOException {
        final String name = "section1";
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path root = tmpDir.resolve(Files.createTempDirectory(tmpDir,".jnote"));
        Path nb1 = root.resolve("nb1");
        Files.createDirectories(nb1);
        NoteBook nb = new NoteBook(nb1);
        nb.createNewEmptySection(name);
        nb.createNewEmptySection(name);

    }

}
