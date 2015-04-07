package org.beynet.jnote.gui.tabs;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.gui.dialogs.Alert;

import java.io.IOException;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection  extends Tab {

    public NoteSection(NoteBookRef noteBookRef,String name,String UUID,String contentStr) {
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
        content = new HTMLEditor();
        if (contentStr!=null && !"".equals(contentStr)) content.setHtmlText(contentStr);
        setContent(content);
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                save();
            } else {

            }
        });
        this.UUID = UUID;
        this.noteBookRef = noteBookRef;
    }

    void save() {
        logger.debug("saving content");
        Controller.saveSectionContent(noteBookRef,UUID,content.getHtmlText());
    }

    public String getUUID() {
        return UUID;
    }
    public void changeName(String name) {
        System.out.println("CHANGING NAME !!!!!!!!!!!");
        labeltitle.setText(name);
        this.setGraphic(labeltitle);
    }

    private HTMLEditor content;
    private String UUID;
    private NoteBookRef noteBookRef;
    private Label labeltitle ;
    private TextField fieldTitle ;

    private final static Logger logger = Logger.getLogger(NoteSection.class);

}
