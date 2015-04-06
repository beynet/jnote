package org.beynet.jnote.model;

import org.beynet.jnote.DefaultTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
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

        NoteSection note = NoteSection.fromZipFile(testFile);
        note.setContent(htmlContent);
        note.save();


        NoteSection note2 = NoteSection.fromZipFile(testFile);
        assertThat(note2,is(note));
    }

    @Test
    public void load() throws IOException {
        final String name = "section1";
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path root = tmpDir.resolve(".jnote");
        Path nb1 = root.resolve("nb1");
        Files.createDirectories(nb1);
        NoteBook nb = new NoteBook(nb1);
        NoteSection section1 = nb.addSection(name);
        assertThat(section1.getName(),is(name));
        section1.setContent("<html><head><title>this is the title</title></head></html>");
        section1.save();

        Model model = new Model(root);
        List<NoteBook> noteBooks = model.getNoteBooks();
        assertThat(Integer.valueOf(noteBooks.size()), is(Integer.valueOf(1)));
        List<NoteSection> sections = noteBooks.get(0).getSections();
        assertThat(Integer.valueOf(sections.size()), is(Integer.valueOf(1)));


    }
}
