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
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.model.events.section.*;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection extends Tab implements Observer,SectionEventVisitor {

    public NoteSection(Stage currentStage,NoteBookRef noteBookRef,String name,String UUID) {
        this.currentStage = currentStage;
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
        content = new JNoteEditor(currentStage);
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
            if (oldValue != null) {
                save(oldValue.getNoteRef());
                content.onNoteUnSelected(oldValue.getNoteRef());
            }
            if (newValue != null) {
                NoteRef noteRef = newValue.getNoteRef();
                logger.debug("change current note");
                content.setDisable(false);
                String contentStr = null;
                try {
                    contentStr = Controller.getNoteContent(noteRef);
                } catch (IOException e) {
                    logger.error("unable to read note content", e);
                    content.setDisable(true);
                }
                if (contentStr != null) {
                    content.setHtmlText(contentStr);
                } else {
                    content.setHtmlText("");
                }
                content.onNoteSelected(newValue.getNoteRef());
            } else {
                content.setDisable(true);
            }
        });




        //saving content when tab change
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                Controller.unSubscribeToNoteSection(noteSectionRef, this);
                save();
                while (noteList.getList().size() > 0) noteList.getList().remove(0);
            } else {
                try {
                    Controller.subscribeToNoteSection(noteSectionRef, this);
                } catch (IllegalArgumentException e) {
                    // section is being removed
                }
            }
        });


        content.setOnDragOver(event -> {
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
        content.setOnDragEntered(event -> {
            if (event.getGestureSource() != content) {
                content.getStyleClass().add("test");
            }
        });

        content.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            logger.debug("on drag dropped");
            NoteRef noteRef = noteList.getSelectionModel().getSelectedItem().getNoteRef();
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file:db.getFiles()) {
                    final File newFile = file;
                    Task<Void> t = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            if (newFile!=null) {
                                try {
                                    Controller.addAttachment(noteRef,newFile.toPath());
                                } catch (IOException e) {
                                    new Alert(currentStage,"unable to attach file "+e.getMessage()).show();
                                } catch (AttachmentAlreadyExistException e) {
                                    new Alert(currentStage,"a file with the same name is already attached to current note").show();
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

    void save() {
        final NoteListItem selectedItem = noteList.getSelectionModel().getSelectedItem();
        if (selectedItem!=null) {
            save(selectedItem.getNoteRef());
        }
    }
    private void save(NoteRef selectedItem) {
        logger.debug("saving note content = " + content.getHtmlText() + " note uuid=" + selectedItem.getUUID());
        if (selectedItem!=null) {
            try {
                Controller.saveNoteContent(noteSectionRef.getNoteBookRef(), noteSectionRef.getUUID(), selectedItem.getUUID(), content.getHtmlText());
            } catch (IOException e) {
                //TODO : show an alert
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

    private JNoteEditor content;
    private NoteSectionRef noteSectionRef;
    private Label labeltitle ;
    private TextField fieldTitle ;
    private Stage currentStage;
    private NoteList noteList;

    private final static Logger logger = Logger.getLogger(NoteSection.class);

}
