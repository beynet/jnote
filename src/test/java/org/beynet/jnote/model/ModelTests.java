package org.beynet.jnote.model;

import org.beynet.jnote.DefaultTest;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by beynet on 06/04/2015.
 */
public class ModelTests extends DefaultTest{
    @Test
    public void saveNote() {
        String htmlContent = "<html></html>";
        NoteSection note = new NoteSection();
        note.setContent(htmlContent);
        String xmlContentAsString = note.getXMLContentAsString();
        Optional<NoteSection> noteSection = NoteSection.fromString(xmlContentAsString);
        noteSection.orElseThrow(()->new RuntimeException("unable to parse string"));
        noteSection.ifPresent((n)->{
            assertThat(n,is(note));
        });
    }

    @Test
    public void load() {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Model model = new Model(Paths.get("/Users/beynet"));
    }
}
