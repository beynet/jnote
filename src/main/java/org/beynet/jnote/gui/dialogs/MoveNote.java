package org.beynet.jnote.gui.dialogs;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.model.Observable;
import org.beynet.jnote.model.Observer;
import org.beynet.jnote.model.events.model.*;
import org.beynet.jnote.model.events.notebook.*;
import org.beynet.jnote.utils.I18NHelper;

import java.io.IOException;
import java.util.Optional;

public class MoveNote extends DialogModal implements Observer, ModelEventVisitor, NoteBookEventVisitor {
    public MoveNote(Stage parent, Double width, Double height, NoteRef note) {
        super(parent, width, height);
        this.note = note ;
        GridPane pane  = new GridPane();
        getRootGroup().getChildren().add(pane);

        Text label = new Text(I18NHelper.getLabelResourceBundle().getString("notebookname"));
        pane.add(label, 0, 0);

        noteBooksList = new ComboBox<>();
        pane.add(noteBooksList, 1, 0);

        noteSectionsList = new ComboBox<>();
        pane.add(noteSectionsList, 2, 0);

        noteBooksList.setOnAction(event -> {
            NoteBookRef value = noteBooksList.getValue();
            noteSectionsList.getItems().clear();
            selectedNoteBook.ifPresent(noteBookRef -> {
                Controller.unSubscribeToNoteBook(noteBookRef,this);
            });
            if (value!=null) {
                selectedNoteBook = Optional.of(value);
                Controller.subscribeToNoteBook(value, this);
            }
            else {
                selectedNoteBook=Optional.empty();
            }
        });

        Button confirm = new Button("OK");
        pane.add(confirm,1,1);
        confirm.setOnAction(event -> {
            this.close();
            NoteSectionRef value = noteSectionsList.getValue();
            if (value!=null) {
                Platform.runLater(() -> {
                    try {
                        Controller.moveNote(note,value);
                    } catch (Exception e) {
                       new Alert(parent,"unable to move",e).show();
                    }
                });
            }
        });
        Platform.runLater(()->Controller.subscribeToModel(this));

        setOnCloseRequest(evt->{
            close();
        });

    }


    @Override
    public void close() {
        System.out.println("closing !!!!!!!!!!!!!!!!!!!");
        selectedNoteBook.ifPresent(noteBookRef -> {
            Controller.unSubscribeToNoteBook(noteBookRef,this);
        });
        Controller.unSubscribeToModel(this);
        super.close();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof NoteBookEvent) {
            ((NoteBookEvent)arg).accept(this);
        }
        else if (arg instanceof ModelEvent) {
            ((ModelEvent)arg).accept(this);
        }
    }

    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {
        this.noteBooksList.getItems().add(new NoteBookRef(newNoteBookEvent.getUUID(),newNoteBookEvent.getName()));
    }

    @Override
    public void visit(OnExitEvent onExitEvent) {

    }

    @Override
    public void visit(NoteBookRenamed noteBookRenamed) {

    }

    @Override
    public void visit(NoteBookDeleted noteBookDeleted) {

    }



    @Override
    public void visit(NoteSectionAdded event) {
        NoteSectionRef noteSectionRef = new NoteSectionRef(selectedNoteBook.get(), event.getUUID(), event.getName());
        noteSectionsList.getItems().add(noteSectionRef);
    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {

    }

    @Override
    public void visit(NoteSectionDeleted noteSectionDeleted) {

    }


    private Optional<NoteBookRef> selectedNoteBook= Optional.empty();
    private NoteRef note;
    private ComboBox<NoteBookRef> noteBooksList;
    private ComboBox<NoteSectionRef>     noteSectionsList;
}
