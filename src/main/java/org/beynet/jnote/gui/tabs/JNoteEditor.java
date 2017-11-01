package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
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
import org.beynet.jnote.utils.Configuration;
import org.beynet.jnote.utils.I18NHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The jnote editor based on HTMLEditor
 * Created by beynet on 21/04/2015.
 */
public class JNoteEditor extends HTMLEditor implements Observer,NoteEventVisitor{
    private ToolBar bottomToolbar ;
    private ToolBar topToolbar;
    private ComboBox fonts;
    private ColorPicker fontColor;
    public JNoteEditor(Stage currentStage,Runnable undo) {
        super();
        this.undo = undo;
        WebView webview = (WebView) lookup("WebView");
        GridPane.setHgrow(webview, Priority.ALWAYS);
        GridPane.setVgrow(webview, Priority.ALWAYS);


        this.currentStage=currentStage;
        getStyleClass().add(Styles.EDITOR);

        // lookup for top toolbar to add some button
        // -----------------------------------------
        Node node = lookup(".bottom-toolbar");
        if (node instanceof ToolBar) {
            bottomToolbar = (ToolBar) node;
            //ObservableList<Node> items = bottomToolbar.getItems();
            /*items.addListener((ListChangeListener<Node>) c -> {
                while (c.next()==true) {
                    if (c.wasAdded()==true) {
                        for (Node n : c.getAddedSubList()) {
                            System.out.println(n);
                        }
                    }
                }
            });*/
        }

        // lookup for top toolbar to add some button
        // -----------------------------------------
        node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            topToolbar = (ToolBar) node;

            attachmentCombo = new ComboBox<>(attachments);
            attachmentCombo.setCellFactory(param -> new AttachmentRefCell());
            attachmentCombo.setPromptText(I18NHelper.getLabelResourceBundle().getString("attachments"));
            topToolbar.getItems().add(attachmentCombo);
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
            topToolbar.getItems().add(insertTable);

            Button copyContent = new Button("A");
            copyContent.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("copyAllContent")));
            copyContent.setOnAction(event -> {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putHtml(getHtmlText());
                clipboard.setContent(content);
            });
            topToolbar.getItems().add(copyContent);

            Button undoButton = new Button("undo");
            undoButton.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("undo")));
            undoButton.setOnAction(event -> {
                if (autosave != null) autosave.setSkipNextSave(true);
                undo.run();
            });
            topToolbar.getItems().add(undoButton);

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
        webview.setContextMenuEnabled(false);
        createContextMenu(webview);
        //webview.setOnContextMenuRequested(e -> getPopupWindow(webview));
        this.autosave = null;

        setOnKeyPressed(event -> {
            if (autosave != null) autosave.setSkipNextSave(true);
//            if (event.isShortcutDown() && event.getText().equalsIgnoreCase("z")) {
//                undo.run();
//            }
        });

        // the second item added to the toolbar is the fonts tool bar
        // ----------------------------------------------------------
        bottomToolbar.getItems().addListener((ListChangeListener<Node>) c -> {
            if (fonts==null) {
                while (c.next()) {
                    if (bottomToolbar.getItems().size()>=2) {
                        fonts = (ComboBox) bottomToolbar.getItems().get(1);
                        // when font observable list is changed
                        // we clear its content and replace it with our own
                        // -------------------------------------------------
                        fonts.itemsProperty().addListener((observable, oldValue, newValue) -> {
                            if (oldValue != newValue) {
                                Platform.runLater(() -> {
                                    updateFontList(fonts, (ObservableList<String>) newValue);
                                });
                            }
                        });
                    }
                }
            }
        });

        //first color picker is the expected
        topToolbar.getItems().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Node node1 : c.getAddedSubList()) {
                        if (fontColor==null && node1 instanceof ColorPicker) {
                            fontColor = (ColorPicker) node1;
                            Platform.runLater(()->{
                                String color = Configuration.getInstance().getPreferredColor();
                                Matcher m = COLOR_PATTERN.matcher(color);
                                if (m.matches()) {
                                    Color def = Color.rgb(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)));
                                    fontColor.getCustomColors().add(def);
                                }
                            });

                        }
                    }
                }
            }

        });

    }
    private void createContextMenu(WebView webView) {
        ContextMenu c = new ContextMenu();
        String script = js.concat(" isCursorInATable();");
        webView.setOnMousePressed(e -> {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (e.getButton() == MouseButton.SECONDARY) {
                System.out.println( webView.getEngine().executeScript("document.elementFromPoint("
                    +e.getX()
                    +"," +  e.getY()+").tagName;"));
                if (Boolean.TRUE.equals(webView.getEngine().executeScript(script))) {
                    // add new item:
                    c.getItems().add(addLineToTable);
                    c.getItems().add(addColToTable);
                }
                else {
                    c.getItems().removeAll(addColToTable,addLineToTable);
                }
                c.show(webView, e.getScreenX(), e.getScreenY());
            } else {
                c.hide();
            }
        });
    }

    protected void updateFontList(ComboBox<String> fonts,ObservableList<String> fontList) {
        for (int i=fontList.size()-1;i>=0;i--) {
            fontList.remove(i);
        }
        int offset = -1;
        int fontOffset = -1 ;
        for (String font : Configuration.getInstance().getFontList()) {
            offset++;
            fontList.add(font);
            if (Configuration.getInstance().getPreferredFont().equals(font)) {
                fontOffset=offset;
            }
        }
        if (fontOffset!=-1) fonts.getSelectionModel().select(fontOffset);
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
   /* private PopupWindow getPopupWindow(WebView webview) {
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
    }*/


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
        if (htmlText==null ||"".equals(htmlText)) {
            htmlText="<body  style=\"color :"+Configuration.getInstance().getPreferredColor()+"; font-family: '"+Configuration.getInstance().getPreferredFont()+"'\"></body>";
        }
        if (autosave!=null) autosave.setSkipNextSave(true);
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


    private final static Pattern COLOR_PATTERN = Pattern.compile("rgb\\((\\d+),(\\d+),(\\d+)\\)");
}
