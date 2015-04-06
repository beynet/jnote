package org.beynet.jnote.gui.tabs;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

/**
 * Created by beynet on 05/04/2015.
 */
public class NoteSection  extends Tab {

    public NoteSection(String title) {
        final Label labeltitle = new Label(title);
        final TextField fieldTitle = new TextField();
        setGraphic(labeltitle);
        labeltitle.setOnMouseClicked((evt) -> {
            if (evt.getClickCount() >= 2) {
                fieldTitle.setText(labeltitle.getText());
                setGraphic(fieldTitle);
            }
        });

        fieldTitle.setOnAction((evt) -> {
            labeltitle.setText(fieldTitle.getText());
            setGraphic(labeltitle);
        });
        content = new HTMLEditor();
        setContent(content);
        setOnSelectionChanged((evt) -> {
            if (isSelected() == false) {
                System.out.println(content.getHtmlText());
            }
        });
    }
    private HTMLEditor content;
}
