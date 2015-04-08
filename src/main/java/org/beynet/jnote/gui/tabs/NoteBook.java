package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.model.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteBook extends TabPane implements Observer ,ModelEventVisitor {
    public NoteBook() {
        this.addNoteTab = new AddNoteTab();
        setOnMouseClicked(event -> {
            checkClick(event);
        });
        getStyleClass().add("tabpane");
        getTabs().add(addNoteTab);
        currentNoteBook = null;
    }


    void changeCurrentNoteBook(NoteBookRef currentNoteBook) {
        getTabs().clear();
        getTabs().add(addNoteTab);
        if (this.currentNoteBook!=null) {
            Controller.unSubscribeToNoteBook(currentNoteBook,this);
        }
        this.currentNoteBook=currentNoteBook;
        if (this.currentNoteBook!=null) {
            Controller.subscribeToNoteBook(currentNoteBook, this);
        }
    }

    private void checkClick(MouseEvent event) {
        final Tab selectedItem = getSelectionModel().getSelectedItem();
        if (addNoteTab.equals(selectedItem)) {
            createNewNoteSection();
        }
    }


    private void addSection(NoteSection section) {
        mySections.add(section);
        getTabs().add(0, section);
        getSelectionModel().selectFirst();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof org.beynet.jnote.model.NoteBook) {
            if (arg!=null) ((ModelEvent)arg).accept(this);
        }
    }

    public void createNewNoteSection() {
        if (currentNoteBook!=null) {
            Controller.createNewSection(currentNoteBook);
        }
    }



    @Override
    public void visit(NoteSectionAdded event) {
        NoteSection section = new NoteSection(currentNoteBook,event.getName(),event.getUUID());
        Platform.runLater(() -> addSection(section));
    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {
        Platform.runLater(()->{
            for (NoteSection section:mySections) {
                if (section.getUUID().equals(sectionRenamed.getUUID())) {
                    section.changeName(sectionRenamed.getName());
                }
            }
        });
    }

    @Override
    public void visit(OnExitEvent onExitEvent) {
        final Tab selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem==null) return;
        if (selectedItem instanceof NoteSection) ((NoteSection)selectedItem).save();
    }

    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {
        // NOT INTERESTED BY IT
    }

    @Override
    public void visit(NoteAdded noteAdded) {

    }

    @Override
    public void visit(NoteRenamed noteRenamed) {

    }

    @Override
    public void visit(NoteContentChanged noteContentChanged) {

    }

    private AddNoteTab addNoteTab;
    private List<NoteSection> mySections = new ArrayList<>();
    private NoteBookRef currentNoteBook ;
}
