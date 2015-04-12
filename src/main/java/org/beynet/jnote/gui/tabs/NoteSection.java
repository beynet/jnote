package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.model.events.model.NewNoteBookEvent;
import org.beynet.jnote.model.events.model.OnExitEvent;
import org.beynet.jnote.model.events.notebook.*;
import org.beynet.jnote.model.events.section.*;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection extends Tab implements Observer,SectionEventVisitor {

    public NoteSection(Stage currentStage,NoteBookRef noteBookRef,String name,String UUID) {
        this.currentStage = currentStage;
        this.UUID = UUID;
        this.noteSectionRef = new NoteSectionRef(noteBookRef,UUID,name);
        labeltitle = new Label(name);
        fieldTitle = new TextField();
        setGraphic(labeltitle);
        labeltitle.setOnMouseClicked((evt) -> {
            if (evt.getClickCount() >= 2) {
                fieldTitle.setText(labeltitle.getText());
                setGraphic(fieldTitle);
            }
        });

        fieldTitle.setOnAction((evt) -> {
            //labeltitle.setText(fieldTitle.getText());
            setGraphic(labeltitle);
            try {
                Controller.changeSectionName(noteBookRef, UUID, fieldTitle.getText());
            } catch (IOException e) {
                logger.error("unable to change section name",e);
                new org.beynet.jnote.gui.dialogs.Alert(currentStage,"unable to modify section name "+e.getMessage()).show();
            }
        });
        HBox hbox = new HBox();


        noteList = new NoteList(this.currentStage,noteSectionRef);
        content = new HTMLEditor();
        content.setDisable(true);
        hbox.getChildren().add(content);
        hbox.getChildren().add(noteList);
        noteList.setPrefWidth(hbox.getWidth() * 15 / 100);
        content.setPrefWidth(hbox.getWidth() - noteList.getWidth());
        hbox.widthProperty().addListener((observable1, oldValue1, newValue1) -> {
            noteList.setPrefWidth(newValue1.doubleValue()*15/100);
            content.setPrefWidth(newValue1.doubleValue() - noteList.getPrefWidth());
        });
        setContent(hbox);

        // enable html content
        // -------------------
        noteList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue!=null) {
                save(oldValue.getNoteRef());
            }
            if (newValue!=null) {
                NoteRef noteRef = newValue.getNoteRef();
                logger.debug("change current note");
                content.setDisable(false);
                if (noteRef.getContent()!=null) {
                    content.setHtmlText(noteRef.getContent());
                }
                else {
                    content.setHtmlText("");
                }
            }
            else {
                content.setDisable(true);
            }
        });




        //saving content when tab change
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                Controller.unSubscribeToNoteSection(noteSectionRef, this);
                save();
                while (noteList.getList().size()>0) noteList.getList().remove(0);
            } else {
                try {
                    Controller.subscribeToNoteSection(noteSectionRef, this);
                }catch(IllegalArgumentException e) {
                    // section is being removed
                }
            }
        });

    }



    public void delete() {
        setOnSelectionChanged(null);
    }


    public boolean match(String uuid) {
        if (noteSectionRef.getUUID().equals(uuid)) return true;
        return false;
    }

    void save() {
        final NoteListItem selectedItem = noteList.getSelectionModel().getSelectedItem();
        if (selectedItem!=null) {
            save(selectedItem.getNoteRef());
        }
    }
    private void save(NoteRef selectedItem) {
        logger.debug("saving note content = "+content.getHtmlText());
        if (selectedItem!=null) {
            try {
                Controller.saveNoteContent(noteSectionRef.getNoteBookRef(), UUID, selectedItem.getUUID(), content.getHtmlText());
            } catch (IOException e) {
                //TODO : show an alert
            }
        }
    }

    public String getUUID() {
        return UUID;
    }
    public void changeName(String name) {
        System.out.println("CHANGING NAME !!!!!!!!!!!");
        labeltitle.setText(name);
        this.setGraphic(labeltitle);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg!=null && arg instanceof NoteBookEvent) {
            ((SectionEvent)arg).accept(this);
        }
    }

    @Override
    public void visit(NoteAdded noteAdded) {
        Platform.runLater(()->{
            if (isSelected() == true) {
                logger.debug("add new note name=" + noteAdded.getName() + " UUID=" + noteAdded.getUUID() + " to section " + noteSectionRef.getSectionName());
                noteList.getList().add(new NoteListItem(new NoteRef(noteSectionRef, noteAdded.getUUID(), noteAdded.getName(), noteAdded.getContent()), false));
            }
        });
    }

    @Override
    public void visit(NoteRenamed noteRenamed) {
        for (NoteListItem n:noteList.getList()) {
            if (n.getNoteRef().getUUID().equals(noteRenamed.getNoteUUID())) {
                n.changeName(noteRenamed.getName());
            }
        }
    }

    @Override
    public void visit(NoteContentChanged noteContentChanged) {
        for (NoteListItem n:noteList.getList()) {
            if (n.getNoteRef().getUUID().equals(noteContentChanged.getNoteUUID())) {
                n.changeContent(noteContentChanged.getContent());
            }
        }
    }

    @Override
    public void visit(NoteDeleted noteDeleted) {
        for (NoteListItem n:noteList.getList()) {
            if (n.getNoteRef().getUUID().equals(noteDeleted.getUUID())) {
                Platform.runLater(()->{
                    noteList.getList().remove(n);
                });
                break;
            }
        }
    }

    private HTMLEditor content;
    private String UUID;
    private NoteSectionRef noteSectionRef;
    private Label labeltitle ;
    private TextField fieldTitle ;
    private Stage currentStage;

    private NoteList noteList;

    private final static Logger logger = Logger.getLogger(NoteSection.class);
}
