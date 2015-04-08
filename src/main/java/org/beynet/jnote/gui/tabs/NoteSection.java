package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.model.Model;
import org.beynet.jnote.model.events.*;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection extends Tab implements Observer,ModelEventVisitor {

    public NoteSection(NoteBookRef noteBookRef,String name,String UUID) {
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
                //TODO : show an alert
                logger.error("unable to modify section name");
            }
        });
        HBox hbox = new HBox();


        noteList = new NoteList(noteSectionRef);
        content = new HTMLEditor();
        content.setDisable(true);
        hbox.getChildren().add(content);
        hbox.getChildren().add(noteList);

        setContent(hbox);


        // enable html content
        // -------------------
        noteList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue!=null) {
                save(oldValue.getNoteRef());
            }
            if (newValue!=null) {
                content.setDisable(false);
                content.setHtmlText(newValue.getNoteRef().getContent());
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
                Controller.subscribeToNoteSection(noteSectionRef, this);
            }
        });

    }

    void save() {
        final NoteListItem selectedItem = noteList.getSelectionModel().getSelectedItem();
        if (selectedItem!=null) {
            save(selectedItem.getNoteRef());
        }
    }
    private void save(NoteRef selectedItem) {
        logger.debug("saving content");
        if (selectedItem!=null) {
            try {
                Controller.saveSectionContent(noteSectionRef.getNoteBookRef(),UUID,selectedItem.getUUID(),content.getHtmlText());
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
        if (arg!=null && arg instanceof ModelEvent) {
            ((ModelEvent)arg).accept(this);
        }
    }


    @Override
    public void visit(NoteSectionAdded event) {

    }

    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {

    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {

    }

    @Override
    public void visit(OnExitEvent onExitEvent) {

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

    private HTMLEditor content;
    private String UUID;
    private NoteSectionRef noteSectionRef;
    private Label labeltitle ;
    private TextField fieldTitle ;

    private NoteList noteList;

    private final static Logger logger = Logger.getLogger(NoteSection.class);
}
