package org.beynet.jnote.gui.tabs;

import com.sun.javafx.scene.control.skin.ContextMenuContent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.gui.dialogs.FileManagement;
import org.beynet.jnote.gui.dialogs.TableSize;
import org.beynet.jnote.model.events.note.AttachmentAddedToNote;
import org.beynet.jnote.model.events.note.AttachmentRemovedFromNote;
import org.beynet.jnote.model.events.note.NoteEvent;
import org.beynet.jnote.model.events.note.NoteEventVisitor;
import org.beynet.jnote.utils.I18NHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by beynet on 21/04/2015.
 */
public class JNoteEditor extends HTMLEditor implements Observer,NoteEventVisitor{
    public JNoteEditor(Stage currentStage,Runnable undo) {
        super();
        this.undo = undo;
        WebView webview = (WebView) lookup("WebView");
        GridPane.setHgrow(webview, Priority.ALWAYS);
        GridPane.setVgrow(webview, Priority.ALWAYS);

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
            insertTable.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("createATableTooltip")));
            insertTable.setOnAction(event -> {
                if (autosave != null) autosave.setSkipNextSave(true);
                if (Boolean.TRUE.equals(webview.getEngine().executeScript(js+"isCursorInATable()"))) {
                    List<String> choice = Arrays.asList(I18NHelper.getLabelResourceBundle().getString("increase"),I18NHelper.getLabelResourceBundle().getString("reduce"));
                    ChoiceDialog<String> increaseOrReduceTableDimension = new ChoiceDialog<String>(choice.get(0),choice);
                    Optional<String> result = increaseOrReduceTableDimension.showAndWait();
                    result.ifPresent((res) -> {
                        if (res.equals(I18NHelper.getLabelResourceBundle().getString("increase"))) {
                            addOrRemoveColOrRow(webview, Boolean.TRUE);
                        }
                        else {
                            addOrRemoveColOrRow(webview, Boolean.FALSE);
                        }
                    });
                }
                else {
                    insertTable();
                }
            });
            bar.getItems().add(insertTable);

            Button copyContent = new Button("A");
            copyContent.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("copyAllContent")));
            copyContent.setOnAction(event -> {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putHtml(getHtmlText());
                clipboard.setContent(content);
            });
            bar.getItems().add(copyContent);

            Button undoButton = new Button("undo");
            undoButton.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("undo")));
            undoButton.setOnAction(event -> {
                if (autosave != null) autosave.setSkipNextSave(true);
                undo.run();
            });
            bar.getItems().add(undoButton);

        }

        try {
            js = readJs();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // adding new item:
        addLineToTable = new MenuItem("add ligne to table");
        addLineToTable.setOnAction(e -> {
            webview.getEngine().executeScript(js+"insertLine()");
        });

        addColToTable = new MenuItem("add col to table");
        addColToTable.setOnAction(e->{
            webview.getEngine().executeScript(js+"insertCol()");
        });

        webview.setOnContextMenuRequested(e -> getPopupWindow(webview));
        this.autosave = null;

        setOnKeyPressed(event -> {
            if (autosave != null) autosave.setSkipNextSave(true);
//            if (event.isShortcutDown() && event.getText().equalsIgnoreCase("z")) {
//                undo.run();
//            }
        });

    }

    // add col or row to current table
    // --------------------------------
    public void addOrRemoveColOrRow(WebView webview, Boolean increase) {
        List<String> choices = Arrays.asList(I18NHelper.getLabelResourceBundle().getString("col"), I18NHelper.getLabelResourceBundle().getString("row"));
        ChoiceDialog<String> choice = new ChoiceDialog<String>(choices.get(0), choices);
        if (Boolean.TRUE.equals(increase)) {
            choice.setTitle(I18NHelper.getLabelResourceBundle().getString("addColumnOrRow"));
            choice.setHeaderText(I18NHelper.getLabelResourceBundle().getString("addColumnOrRow"));
        }
        else {
            choice.setTitle(I18NHelper.getLabelResourceBundle().getString("removeColumnOrRow"));
            choice.setHeaderText(I18NHelper.getLabelResourceBundle().getString("removeColumnOrRow"));
        }
        Optional<String> result = choice.showAndWait();
        result.ifPresent((res) -> {
            if (res.equals(I18NHelper.getLabelResourceBundle().getString("col"))) {
                if (Boolean.TRUE.equals(increase)) {
                    webview.getEngine().executeScript(js + "insertCol();");
                }
                else {
                    webview.getEngine().executeScript(js + "removeCol();");
                }
            } else {
                if (Boolean.TRUE.equals(increase)) {
                    webview.getEngine().executeScript(js + "insertLine();");
                } else {
                    webview.getEngine().executeScript(js + "removeLine();");
                }
            }
        });

    }
    private PopupWindow getPopupWindow(WebView webview) {
        @SuppressWarnings("deprecation")
        final Iterator<Window> windows = Window.impl_getWindows();

        while (windows.hasNext()) {
            final Window window = windows.next();

            if (window instanceof ContextMenu) {
                if(window.getScene()!=null && window.getScene().getRoot()!=null){
                    Parent root = window.getScene().getRoot();

                    // access to context menu content
                    if(root.getChildrenUnmodifiable().size()>0){
                        Node popup = root.getChildrenUnmodifiable().get(0);
                        if(popup.lookup(".context-menu")!=null){
                            Node bridge = popup.lookup(".context-menu");
                            ContextMenuContent cmc= (ContextMenuContent)((Parent)bridge).getChildrenUnmodifiable().get(0);

                            VBox itemsContainer = cmc.getItemsContainer();
                            for (Node node : itemsContainer.getChildren()) {
                                if (addLineToTable==((ContextMenuContent.MenuItemContainer)node).getItem()) {
                                    itemsContainer.getChildren().remove(node);
                                    break;
                                }
                                else if (addColToTable==((ContextMenuContent.MenuItemContainer)node).getItem()) {
                                    itemsContainer.getChildren().remove(node);
                                    break;
                                }
                            }
                            if (Boolean.TRUE.equals(webview.getEngine().executeScript(js+" isCursorInATable();"))) {
                                // add new item:
                                itemsContainer.getChildren().add(cmc.new MenuItemContainer(addLineToTable));
                                itemsContainer.getChildren().add(cmc.new MenuItemContainer(addColToTable));
                            }

                            return (PopupWindow)window;
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }


    private String readJs() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[1014];
        try (InputStream jsStream = JNoteEditor.class.getResourceAsStream("/editor.js")) {
            int read = 1;
            while(read>=0) {
                read=jsStream.read(buffer);
                if (read>0){
                    bo.write(buffer,0,read);
                }
            }
        }
        return new String(bo.toByteArray(),"UTF-8");
    }

    private void insertTable() {
        StringBuilder table= new StringBuilder("<table border=\\\"1\\\" cellpadding=\\\"1\\\" cellspacing=\\\"0\\\">");
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

    @Override
    public void setHtmlText(String htmlText) {
        autosave.setSkipNextSave(true);
        super.setHtmlText(htmlText);
        if (autosave!=null) autosave.setLastHtml(htmlText);
    }

    @Override
    public void requestFocus() {
        Platform.runLater(()-> {

            super.requestFocus();
            WebView webview = (WebView) lookup("WebView");
            webview.getEngine().executeScript(js + "focus()");
        });
    }

    /**
     * called when current note is unselected
     * @param noteRef
     */
    public void onNoteUnSelected(NoteRef noteRef) {
        autosave.save();
        Controller.unSubscribeToNote(noteRef, this);
        while (attachments.size()>0) attachments.remove(0);
        currentNoteRef = null;
        if (autosave!=null) autosave.setLastHtml(null);
    }

    public void onNoteSelected(NoteRef noteRef) {
        currentNoteRef = noteRef;
        Controller.subscribeToNote(noteRef,this);
        if (autosave!=null) autosave.setLastHtml(getHtmlText());
    }

    private void save() {
        logger.debug("saving note content = " + getHtmlText() + " note uuid=" +currentNoteRef.getUUID());
        try {
            Controller.saveNoteContent(currentNoteRef.getNoteSectionRef().getNoteBookRef(), currentNoteRef.getNoteSectionRef().getUUID(), currentNoteRef.getUUID(), getHtmlText());
        } catch (IOException e) {
            logger.error("error saving note",e);
        }
    }

    private ObservableList<AttachmentRef> attachments = FXCollections.observableArrayList();
    private ComboBox<AttachmentRef> attachmentCombo ;
    private NoteRef currentNoteRef=null;
    private String js;
    private Stage currentStage;
    private MenuItem addLineToTable;
    private MenuItem addColToTable;
    private AutoSave autosave;
    private Runnable undo;

    private final static Logger logger = Logger.getLogger(JNoteEditor.class);

    /**
     * start autosave thread associated with current editor
     */
    public void startAutosave() {
        if (autosave!=null) {
            logger.warn("autosave already started");
            return;
        }
        autosave = new AutoSave(this::save,this::getHtmlText,this::isDisable);
        autosave.start();
    }

    /**
     * stop autosave thread associated with current editor
     */
    public void stopAutosave() {
        if (autosave==null) {
            logger.warn("autosave already stopped");
            return;
        }
        autosave.interrupt();
        try {
            autosave.join();
            autosave=null;
        } catch (InterruptedException e) {

        }
    }
}
