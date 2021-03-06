package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.model.events.model.*;
import org.beynet.jnote.utils.I18NHelper;
import org.beynet.jnote.model.Observer;
import org.beynet.jnote.model.Observable;

import java.io.IOException;
import java.util.List;

/**
 * Created by beynet on 07/04/15.
 */
public class MainPanel extends VBox implements Observer,ModelEventVisitor {
    public MainPanel(Stage currentStage) {
        this.currentStage=currentStage;
        notes=new NoteBook(this.currentStage);

        // this hbox will contain note books list and search menu
        GridPane centerPane = new GridPane();
        centerPane.setPrefWidth(currentStage.getWidth());
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.LEFT);
        col1.setPercentWidth(50);
        centerPane.getColumnConstraints().add(col1);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.RIGHT);
        col2.setPercentWidth(50);
        centerPane.getColumnConstraints().add(col2);





        n = new NoteBookList(currentStage,notes);
        centerPane.add(n,0,0);

        // create search control
        // **********************
        search = new TextField();
        search.setPrefColumnCount(40);
        search.setPromptText(I18NHelper.getLabelResourceBundle().getString("search"));
        search.setAlignment(Pos.BASELINE_RIGHT);
        searchTooltip = new Tooltip();

        SearchResultsList lst= new SearchResultsList(currentStage,n,notes,searchTooltip);
        searchTooltip.setGraphic(lst);
        searchTooltip.getStyleClass().add(Styles.SEARCH_RESULTS);
        searchTooltip.setAutoHide(true);
        searchTooltip.setHideOnEscape(true);
//        search.setTooltip(searchTooltip);
        // on action we display the tooltip
        search.setOnKeyPressed(event -> {
            if (!KeyCode.ENTER.equals(event.getCode())) {
                searchTooltip.hide();
            }
        });
        search.setOnAction(event -> {
            String text = search.getText();
            if (text != null && !"".equals(text)) {
                try {
                    while (lst.getItems().size() > 0) lst.getItems().remove(0);
                    List<NoteRef> result = Controller.getMatchingNotes(text);
                    if (result != null) {
                        for (NoteRef ref : result) {
                            lst.getItems().add(ref);
                        }
                        Point2D p = search.localToScreen(0.0, 0.0);

                        search.setTooltip(searchTooltip);
                        searchTooltip.show(currentStage, p.getX() + search.getWidth()
                            , p.getY() + search.getHeight() + 5
                        );
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(currentStage, I18NHelper.getLabelResourceBundle().getString("errorRunningSearch"),e).show());
                }
            }
        });
        searchTooltip.setOnHidden(event -> {
            search.setTooltip(null);
        });
        centerPane.add(search, 1, 0);

        getChildren().add(centerPane);
        getChildren().add(notes);


        // binding dimensions
        centerPane.prefWidthProperty().bind(widthProperty());
        notes.prefWidthProperty().bind(widthProperty());
        notes.setPrefHeight(getHeight() - centerPane.getHeight());
        heightProperty().addListener((observable, oldValue, newValue) -> {
            notes.setPrefHeight(newValue.doubleValue()-centerPane.getHeight());
        });

        Controller.subscribeToModel(this);
    }

    public String getSelectedNoteBookUUID() {
        return n.getSelected().getUUID();
    }


    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(()->((ModelEvent) arg).accept(this));
    }


    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {
        n.getItems().add(new NoteBookRef(newNoteBookEvent.getUUID(), newNoteBookEvent.getName()));
    }

    @Override
    public void visit(NoteBookDeleted noteBookDeleted) {
        for (NoteBookRef noteBookRef : n.getItems()) {
            if (noteBookRef.getUUID().equals(noteBookDeleted.getUUID())) {
                n.getItems().remove(noteBookRef);
                break;
            }
        }
    }

    @Override
    public void visit(OnExitEvent onExitEvent) {
        notes.onExit();
    }

    @Override
    public void visit(NoteBookRenamed noteBookRenamed) {
        for (NoteBookRef noteBookRef : n.getItems()) {
            if (noteBookRef.getUUID().equals(noteBookRenamed.getUUID())) {
                noteBookRef.changeName(noteBookRenamed.getCurrent());
                break;
            }
        }

    }


    private NoteBookList n;
    /*private ObservableList<NoteBookRef> noteBooksList ;
    private ComboBox<NoteBookRef> noteBooks ;*/
    private NoteBook notes;

    private Stage currentStage;
    private TextField search;
    private Tooltip searchTooltip;

    public String getSelectedNoteBookName() {
        return n.getSelected()!=null?n.getSelected().getName():null;
    }
}
