package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.model.events.model.ModelEvent;
import org.beynet.jnote.model.events.model.ModelEventVisitor;
import org.beynet.jnote.model.events.model.NewNoteBookEvent;
import org.beynet.jnote.model.events.model.OnExitEvent;
import org.beynet.jnote.model.events.notebook.*;
import org.beynet.jnote.model.events.section.NoteAdded;
import org.beynet.jnote.model.events.section.NoteContentChanged;
import org.beynet.jnote.model.events.section.NoteDeleted;
import org.beynet.jnote.model.events.section.NoteRenamed;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 07/04/15.
 */
public class MainPanel extends VBox implements Observer,ModelEventVisitor {
    public MainPanel(Stage currentStage) {
        this.currentStage=currentStage;
        selected =null;
        notes=new NoteBook(this.currentStage);

        // binding dimensions
        notes.setPrefWidth(getWidth());
        widthProperty().addListener((observable, oldValue, newValue) -> {
            notes.setPrefWidth(getWidth());
        });
        // binding dimensions
        notes.setPrefHeight(getHeight());
        heightProperty().addListener((observable, oldValue, newValue) -> {
            notes.setPrefHeight(getHeight());
        });


        noteBooksList = FXCollections.observableArrayList();
        noteBooks=new ComboBox<>(noteBooksList);
        getChildren().add(noteBooks);
        getChildren().add(notes);

        Controller.subscribeToModel(this);
        noteBooks.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selected = noteBooks.getValue();
                notes.changeCurrentNoteBook(selected);
            }
        });
    }

    public String getSelectedNoteBookUUID() {
        return selected.getUUID();
    }


    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(()->((ModelEvent) arg).accept(this));
    }


    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {
        noteBooksList.add(new NoteBookRef(newNoteBookEvent.getUUID(),newNoteBookEvent.getName()));
    }

    @Override
    public void visit(OnExitEvent onExitEvent) {
        notes.onExit();
    }


    private ObservableList<NoteBookRef> noteBooksList ;
    private ComboBox<NoteBookRef> noteBooks ;
    private NoteBook notes;
    private NoteBookRef selected;
    private Stage currentStage;
}
