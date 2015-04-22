package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.model.events.model.NewNoteBookEvent;
import org.beynet.jnote.model.events.model.OnExitEvent;
import org.beynet.jnote.model.events.notebook.*;
import org.beynet.jnote.model.events.section.NoteAdded;
import org.beynet.jnote.model.events.section.NoteContentChanged;
import org.beynet.jnote.model.events.section.NoteDeleted;
import org.beynet.jnote.model.events.section.NoteRenamed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteBook extends TabPane implements Observer ,NoteBookEventVisitor {
    public NoteBook(Stage currentStage) {
        this.currentStage=currentStage;
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
        if (arg instanceof NoteBookEvent) {
            Platform.runLater(()->{if (arg!=null) ((NoteBookEvent)arg).accept(this);});
        }
    }

    public void createNewNoteSection() {
        if (currentNoteBook!=null) {
            try {
                Controller.createNewSection(currentNoteBook);
            } catch (IOException e) {
                new Alert(currentStage,"unable to create new section "+e.getMessage()).show();
            }
        }
    }


    @Override
    public void visit(NoteSectionDeleted noteSectionDeleted) {
        for (Tab tab:getTabs()) {
            if (tab instanceof NoteSection) {
                NoteSection noteSection = (NoteSection) tab;
                if (noteSection.match(noteSectionDeleted.getUUID())) {
                    noteSection.delete();
                    getTabs().remove(tab);
                    if (getTabs().size()==1) addNoteTab.skipNextSelectionChange();
                    break;
                }
            }
        }
    }

    @Override
    public void visit(NoteSectionAdded event) {
        NoteSection section = new NoteSection(currentStage,currentNoteBook,event.getName(),event.getUUID());
        addSection(section);
    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {
        for (NoteSection section:mySections) {
            if (section.getUUID().equals(sectionRenamed.getUUID())) {
                section.changeName(sectionRenamed.getName());
            }
        }
    }


    public void onExit() {
        final Tab selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem==null) return;
        if (selectedItem instanceof NoteSection) ((NoteSection)selectedItem).save();
    }



    private AddNoteTab addNoteTab;
    private Stage currentStage;
    private List<NoteSection> mySections = new ArrayList<>();
    private NoteBookRef currentNoteBook ;
}
