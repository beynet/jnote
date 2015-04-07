package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.model.Model;
import org.beynet.jnote.model.events.*;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection extends Tab implements Observer,ModelEventVisitor {

    public NoteSection(NoteBookRef noteBookRef,String name,String UUID) {
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
                Controller.changeSectionName(noteBookRef,UUID,fieldTitle.getText());
            } catch (IOException e) {
                //TODO : show an alert
                logger.error("unable to modify section name");
            }
        });
        HBox hbox = new HBox();

        list = FXCollections.observableArrayList();
        listView = new ListView<>(list);
        content = new HTMLEditor();
        content.setDisable(true);
        hbox.getChildren().add(content);
        hbox.getChildren().add(listView);


        setContent(hbox);

        //saving content when tab change
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                save();
                Controller.unSubscribeToNoteSection(noteSectionRef, this);
                while (list.size()>0) list.remove(0);

            } else {
                Controller.subscribeToNoteSection(noteSectionRef, this);
            }
        });

        //enable html content
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<NoteRef>() {
            @Override
            public void changed(ObservableValue<? extends NoteRef> observable, NoteRef oldValue, NoteRef newValue) {
                if (oldValue!=null) {
                    save(oldValue);
                }
                if (newValue!=null) {
                    content.setDisable(false);
                    content.setHtmlText(newValue.getContent());
                }
                else {
                    content.setDisable(true);
                }
            }
        });


        this.UUID = UUID;
        this.noteSectionRef = new NoteSectionRef(noteBookRef,UUID,name);

    }

    void save() {
        final NoteRef selectedItem = listView.getSelectionModel().getSelectedItem();
        save(selectedItem);
    }
    private void save(NoteRef selectedItem) {
        logger.debug("saving content");
        if (selectedItem!=null) Controller.saveSectionContent(noteSectionRef.getNoteBookRef(),UUID,selectedItem.getUUID(),content.getHtmlText());
    }

    public String getUUID() {
        return UUID;
    }
    public void changeName(String name) {
        System.out.println("CHANGING NAME !!!!!!!!!!!");
        labeltitle.setText(name);
        this.setGraphic(labeltitle);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg!=null && arg instanceof ModelEvent) {
            ((ModelEvent)arg).accept(this);
        }
    }


    @Override
    public void visit(NoteSectionAdded event) {

    }

    @Override
    public void visit(NewNoteBookEvent newNoteBookEvent) {

    }

    @Override
    public void visit(SectionRenamed sectionRenamed) {

    }

    @Override
    public void visit(OnExitEvent onExitEvent) {

    }

    @Override
    public void visit(NoteAdded noteAdded) {
        Platform.runLater(()->{
            list.add(new NoteRef(noteAdded.getUUID(),noteAdded.getName(),noteAdded.getContent()));
        });
    }


    private HTMLEditor content;
    private String UUID;
    private NoteSectionRef noteSectionRef;
    private Label labeltitle ;
    private TextField fieldTitle ;
    private ObservableList<NoteRef> list ;
    private ListView<NoteRef> listView;

    private final static Logger logger = Logger.getLogger(NoteSection.class);
}
