package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.gui.dialogs.FileManagement;
import org.beynet.jnote.gui.dialogs.TableSize;
import org.beynet.jnote.model.events.note.AttachmentAddedToNote;
import org.beynet.jnote.model.events.note.AttachmentRemovedFromNote;
import org.beynet.jnote.model.events.note.NoteEvent;
import org.beynet.jnote.model.events.note.NoteEventVisitor;
import org.beynet.jnote.utils.I18NHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 21/04/2015.
 */
public class JNoteEditor extends HTMLEditor implements Observer,NoteEventVisitor{
    public JNoteEditor(Stage currentStage) {
        super();
        this.currentStage=currentStage;
        getStyleClass().add(Styles.EDITOR);
        Node node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            ToolBar bar = (ToolBar) node;
            attachmentCombo = new ComboBox<>(attachments);
            attachmentCombo.setCellFactory(param -> new AttachmentRefCell());
            attachmentCombo.setPromptText(I18NHelper.getLabelResourceBundle().getString("attachments"));
            bar.getItems().add(attachmentCombo);
            attachmentCombo.setOnAction(event -> {
                final AttachmentRef selected = attachmentCombo.getSelectionModel().getSelectedItem();
                if (selected!=null) {
                    Platform.runLater(()-> {
                        attachmentCombo.getSelectionModel().select(null);
                        new FileManagement(currentStage, selected).show();
                    });
                }
            });

            Button insertTable = new Button("table");
            insertTable.setOnAction(event -> {
                insertTable();
            });
            bar.getItems().add(insertTable);

        }
        WebView webview = (WebView) lookup("WebView");
        GridPane.setHgrow(webview, Priority.ALWAYS);
        GridPane.setVgrow(webview, Priority.ALWAYS);
        try {
            js = new String(Files.readAllBytes(Paths.get(JNoteEditor.class.getResource("/editor.js").toURI())),"UTF-8");

        } catch (IOException |URISyntaxException e) {
           throw new RuntimeException(e);
        }

    }

    private void insertTable() {
        StringBuilder table= new StringBuilder("<table border=\\\"1\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\">");
        final TableSize tableSize = new TableSize(currentStage);
        tableSize.showAndWait();
        if (Boolean.TRUE.equals(tableSize.isValidated())) {
            for (int r = 0; r < tableSize.getRowSize(); r++) {
                table.append("<tr>");
                for (int c = 0; c < tableSize.getColSize(); c++) {
                    table.append("<td>&nbsp;</td>");
                }
                table.append("</tr>");
            }
            table.append("</table>");
            logger.info("inserting table " + table);
            WebView webview = (WebView) lookup("WebView");
            webview.getEngine().executeScript(js + "\ninsertHtmlAtCursor('" + table.toString() + "');");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg!=null && arg instanceof NoteEvent) {
            Platform.runLater(() -> ((NoteEvent) arg).accept(this));
        }
    }

    @Override
    public void visit(AttachmentAddedToNote attachmentAddedToNote) {
        AttachmentRef attachmentRef = new AttachmentRef(currentNoteRef, attachmentAddedToNote.getFileName(), attachmentAddedToNote.getSize());
        attachments.add(attachmentRef);
    }

    @Override
    public void visit(AttachmentRemovedFromNote attachmentRemovedFromNote) {
        for (AttachmentRef ref:attachments) {
            if (ref.getFileName().equals(attachmentRemovedFromNote.getFileName())) {
                attachments.remove(ref);
                break;
            }
        }
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
    private String js;
    private Stage currentStage;

    private final static Logger logger = Logger.getLogger(JNoteEditor.class);
}
