package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.model.events.notebook.*;
import org.beynet.jnote.utils.I18NHelper;

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
        getStyleClass().add(Styles.TAB_PANE);
        getTabs().add(addNoteTab);
        currentNoteBook = null;
    }


    void changeCurrentNoteBook(NoteBookRef currentNoteBook) {
        getTabs().clear();
        getTabs().add(addNoteTab);
        if (this.currentNoteBook!=null) {
            Controller.unSubscribeToNoteBook(this.currentNoteBook,this);
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
                new Alert(currentStage, I18NHelper.getLabelResourceBundle().getString("errorCreatingNewSection")+e.getMessage(),e).show();
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

    public void selectSectionAndNote(NoteRef noteRef) {
        for (Tab tab:getTabs()) {
            if (tab instanceof NoteSection) {
                NoteSection section = (NoteSection)tab;
                if (section.getUUID().equals(noteRef.getNoteSectionRef().getUUID())) {
                    getSelectionModel().select(section);
                    Platform.runLater(()->section.selectNote(noteRef));
                    break;
                }
            }
        }
    }


    public void onExit() {
        final Tab selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem==null) return;
        if (selectedItem instanceof NoteSection) {
            ((NoteSection)selectedItem).stopAutosave();
        }
    }



    private AddNoteTab addNoteTab;
    private Stage currentStage;
    private List<NoteSection> mySections = new ArrayList<>();
    private NoteBookRef currentNoteBook ;
}
