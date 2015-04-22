package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.dialogs.FileManagement;
import org.beynet.jnote.model.events.note.AttachmentAddedToNote;
import org.beynet.jnote.model.events.note.AttachmentRemovedFromNote;
import org.beynet.jnote.model.events.note.NoteEvent;
import org.beynet.jnote.model.events.note.NoteEventVisitor;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 21/04/2015.
 */
public class JNoteEditor extends HTMLEditor implements Observer,NoteEventVisitor{
    public JNoteEditor(Stage currentStage) {
        super();
        Node node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            ToolBar bar = (ToolBar) node;
            attachmentCombo = new ComboBox<>(attachments);
            attachmentCombo.setCellFactory(param -> new AttachmentRefCell());
            attachmentCombo.setPromptText("attachments");
            bar.getItems().add(attachmentCombo);
            attachmentCombo.setOnAction(event -> {
                final AttachmentRef selected = attachmentCombo.getSelectionModel().getSelectedItem();
                if (selected!=null) {
                    Platform.runLater(() -> {
                        attachmentCombo.getSelectionModel().select(null);
                        new FileManagement(currentStage, selected).show();
                    });
                }
            });
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg!=null && arg instanceof NoteEvent) {
            ((NoteEvent) arg).accept(this);
        }
    }

    @Override
    public void visit(AttachmentAddedToNote attachmentAddedToNote) {
        AttachmentRef attachmentRef = new AttachmentRef(currentNoteRef, attachmentAddedToNote.getFileName(), attachmentAddedToNote.getSize());
        attachments.add(attachmentRef);
    }

    @Override
    public void visit(AttachmentRemovedFromNote attachmentRemovedFromNote) {
        Platform.runLater(()->{
            for (AttachmentRef ref:attachments) {
                if (ref.getFileName().equals(attachmentRemovedFromNote.getFileName())) {
                    attachments.remove(ref);
                    break;
                }
            }
        });
    }

    /**
     * called when current note is unselected
     * @param noteRef
     */
    public void onNoteUnSelected(NoteRef noteRef) {
        Controller.unSubscribeToNote(noteRef, this);
        while (attachments.size()>0) attachments.remove(0);
        currentNoteRef = null;
    }

    public void onNoteSelected(NoteRef noteRef) {
        currentNoteRef = noteRef;
        Controller.subscribeToNote(noteRef,this);
    }

    private ObservableList<AttachmentRef> attachments = FXCollections.observableArrayList();
    private ComboBox<AttachmentRef> attachmentCombo ;
    private NoteRef currentNoteRef=null;
}
