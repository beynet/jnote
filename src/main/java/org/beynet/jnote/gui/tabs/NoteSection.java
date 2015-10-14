package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.gui.dialogs.Confirm;
import org.beynet.jnote.model.events.section.*;
import org.beynet.jnote.utils.I18NHelper;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection extends Tab implements Observer,SectionEventVisitor {

    private Boolean justSelected=Boolean.TRUE;

    public NoteSection(Stage currentStage,NoteBookRef noteBookRef,String name,String UUID) {
        this.currentStage = currentStage;
        this.noteSectionRef = new NoteSectionRef(noteBookRef,UUID,name);
        labeltitle = new Label(name);
        fieldTitle = new TextField();
        fieldTitle.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                setGraphic(labeltitle);
            }
        });
        setGraphic(labeltitle);
        getStyleClass().add(Styles.TAB);
        labeltitle.setOnMouseClicked((evt) -> {
            if (evt.getClickCount() >= 1) {
                if (Boolean.FALSE.equals(justSelected)) {
                    fieldTitle.setText(labeltitle.getText());
                    setGraphic(fieldTitle);
                    fieldTitle.requestFocus();
                }
                else {
                    justSelected=Boolean.FALSE;
                }
            }
        });


        setOnCloseRequest(event -> {
            Confirm confirmDeleteSection = new Confirm(currentStage, I18NHelper.getLabelResourceBundle().getString("confirmDeleteSection"));
            confirmDeleteSection.showAndWait();
            if (confirmDeleteSection.isConfirmed()) {
                try {
                    Controller.deleteSection(noteSectionRef);
                } catch (IOException e) {
                    new Alert(currentStage,I18NHelper.getLabelResourceBundle().getString("errorDeletingSection")+e.getMessage(),e).show();
                }
            }
            event.consume();
        });

        fieldTitle.setOnAction((evt) -> {
            //labeltitle.setText(fieldTitle.getText());
            setGraphic(labeltitle);
            try {
                Controller.changeSectionName(noteBookRef, UUID, fieldTitle.getText());
            } catch (IOException e) {
                logger.error("unable to change section name",e);
                new Alert(currentStage,I18NHelper.getLabelResourceBundle().getString("errorModyfyingSectionName")+e.getMessage(),e).show();
            }
        });
        HBox hbox = new HBox();
        hbox.getStyleClass().add(Styles.TAB_CONTENT);

        noteList = new NoteList(this.currentStage,noteSectionRef);
        editor = new JNoteEditor(currentStage, this::undo);
        editor.setDisable(true);
        hbox.getChildren().add(editor);
        hbox.getChildren().add(noteList);
        noteList.setPrefWidth(hbox.getWidth() * 15 / 100);
        editor.setPrefWidth(hbox.getWidth() - noteList.getWidth());
        hbox.widthProperty().addListener((observable1, oldValue1, newValue1) -> {
            noteList.setPrefWidth(newValue1.doubleValue()*15/100);
            editor.setPrefWidth(newValue1.doubleValue() - noteList.getPrefWidth());
        });

        editor.prefHeightProperty().bind(hbox.heightProperty().subtract(noteList.heightProperty()));

        setContent(hbox);

        // enable html content
        // -------------------
        noteList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                if (noteList.getList().contains(oldValue)) {
                    editor.onNoteUnSelected(oldValue.getNoteRef());
                }
            }
            if (newValue != null) {
                NoteRef noteRef = newValue.getNoteRef();
                logger.debug("change current note");
                editor.setDisable(false);
                String contentStr = null;
                try {
                    contentStr = Controller.getNoteContent(noteRef);
                } catch (IOException e) {
                    logger.error("unable to read note content", e);
                    editor.setDisable(true);
                }
                if (contentStr != null) {
                    editor.setHtmlText(contentStr);
                } else {
                    editor.setHtmlText("");
                }
                editor.onNoteSelected(newValue.getNoteRef());
            } else {
                editor.setDisable(true);
            }
        });




        //saving content when tab change
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                editor.stopAutosave();
                setGraphic(labeltitle);
                Controller.unSubscribeToNoteSection(noteSectionRef, this);
                while (noteList.getList().size() > 0) noteList.getList().remove(0);
            } else {
                justSelected = Boolean.TRUE;
                try {
                    Controller.subscribeToNoteSection(noteSectionRef, this);
                } catch (IllegalArgumentException e) {
                    // section is being removed
                }
                editor.startAutosave();
            }
        });


        editor.setOnDragOver(event -> {
            logger.debug("start drag over");
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                logger.debug("accepting drag over");
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                logger.debug("consuming drag over");
                event.consume();
            }
        });
        editor.setOnDragEntered(event -> {
            if (event.getGestureSource() != editor) {
                editor.getStyleClass().add("test");
            }
        });

        editor.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            logger.debug("on drag dropped");
            NoteRef noteRef = noteList.getSelectionModel().getSelectedItem().getNoteRef();
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file : db.getFiles()) {
                    final File newFile = file;
                    Task<Void> t = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            if (newFile != null) {
                                try {
                                    Controller.addAttachment(noteRef, newFile.toPath());
                                } catch (IOException e) {
                                    new Alert(currentStage, I18NHelper.getLabelResourceBundle().getString("errorAttachingFile") + e.getMessage(), e).show();
                                } catch (AttachmentAlreadyExistException e) {
                                    new Alert(currentStage, I18NHelper.getLabelResourceBundle().getString("errorFileWithSameName")).show();
                                }
                            }
                            return null;
                        }
                    };
                    new Thread(t).start();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

    }



    public void delete() {
        setOnSelectionChanged(null);
    }


    public boolean match(String uuid) {
        if (noteSectionRef.getUUID().equals(uuid)) return true;
        return false;
    }

    void undo() {
        logger.info("undo");
        final NoteListItem selectedItem = noteList.getSelectionModel().getSelectedItem();
        if (selectedItem!=null) {
            try {
                Controller.undoNoteContent(noteSectionRef.getNoteBookRef(), noteSectionRef.getUUID(), selectedItem.getNoteRef().getUUID(),editor.getHtmlText());
            } catch (IOException e) {

            }
        }
    }





    public String getUUID() {
        return noteSectionRef.getUUID();
    }
    public void changeName(String name) {
        System.out.println("CHANGING NAME !!!!!!!!!!!");
        labeltitle.setText(name);
        this.setGraphic(labeltitle);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg!=null && arg instanceof SectionEvent) {
            Platform.runLater(()->((SectionEvent) arg).accept(this));
        }
    }

    @Override
    public void visit(NoteAdded noteAdded) {
        if (isSelected() == true) {
            logger.debug("add new note name=" + noteAdded.getName() + " UUID=" + noteAdded.getUUID() + " to section " + noteSectionRef.getSectionName());
            if (!noteList.containsNote( noteAdded.getUUID())) {
                noteList.getList().add(new NoteListItem(new NoteRef(noteSectionRef, noteAdded.getUUID(), noteAdded.getName()), false));
            }
        }
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

    }

    @Override
    public void visit(NoteContentUndo noteContentUndo) {
        editor.setHtmlText(noteContentUndo.getContent());
        Platform.runLater(()->editor.requestFocus());
    }

    @Override
    public void visit(NoteDeleted noteDeleted) {
        for (NoteListItem n:noteList.getList()) {
            if (n.getNoteRef().getUUID().equals(noteDeleted.getUUID())) {
                noteList.getList().remove(n);
                break;
            }
        }
    }

    /**
     * select requested note
     * @param noteRef
     */
    public void selectNote(NoteRef noteRef) {
        for (NoteListItem noteListItem : noteList.getItems()) {
            if (noteListItem.getNoteRef().getUUID().equals(noteRef.getUUID())) {
                noteList.getSelectionModel().select(noteListItem);
            }
        }
    }

    /**
     * request editor to stop autosave
     */
    public void stopAutosave() {
        editor.stopAutosave();
    }

    private JNoteEditor editor;
    private NoteSectionRef noteSectionRef;
    private Label labeltitle ;
    private TextField fieldTitle ;
    private Stage currentStage;
    private NoteList noteList;

    private final static Logger logger = Logger.getLogger(NoteSection.class);


}
