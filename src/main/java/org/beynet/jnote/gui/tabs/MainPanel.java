package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.model.events.*;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 07/04/15.
 */
public class MainPanel extends VBox implements Observer,ModelEventVisitor{
    public MainPanel() {
        selected =null;
        notes=new NoteBook();
        noteBooksList = FXCollections.observableArrayList();
        noteBooks=new ComboBox<>(noteBooksList);
        getChildren().add(noteBooks);
        getChildren().add(notes);

        Controller.subscribeToModel(this);
        noteBooks.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selected=noteBooks.getValue();
                notes.changeCurrentNoteBook(selected);
            }
        });
    }


    @Override
    public void update(Observable o, Object arg) {
        ((ModelEvent)arg).accept(this);
    }

    @Override
    public void visit(NoteSectionAdded event) {
        // NOTHING TO DO
    }

    @Override
    public void visit(NoteRenamed noteRenamed) {

    }

    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {
        Platform.runLater(()->{
            noteBooksList.add(new NoteBookRef(newNoteBookEvent.getUUID(),newNoteBookEvent.getName()));
        });
    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {
        // NOTHING TO DO
    }

    @Override
    public void visit(OnExitEvent onExitEvent) {
        notes.visit(onExitEvent);
    }

    @Override
    public void visit(NoteAdded noteAdded) {

    }

    @Override
    public void visit(NoteContentChanged noteContentChanged) {

    }

    private ObservableList<NoteBookRef> noteBooksList ;
    private ComboBox<NoteBookRef> noteBooks ;
    private NoteBook notes;
    private NoteBookRef selected;

}
