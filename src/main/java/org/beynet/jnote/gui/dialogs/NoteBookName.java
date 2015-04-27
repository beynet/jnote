package org.beynet.jnote.gui.dialogs;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.utils.I18NHelper;

import java.io.IOException;

/**
 * Created by beynet on 11/04/2015.
 */
public class NoteBookName extends DialogModal{
    public NoteBookName(Stage parent,Double width,Double height) {
        this(null,null,parent,width,height);
    }
    public NoteBookName(String currentUUID,String currentName,Stage parent,Double width,Double height) {
        super(parent, width, height);
        this.currentUUID = currentUUID;
        GridPane pane  = new GridPane();
        getRootGroup().getChildren().add(pane);

        Text label = new Text(I18NHelper.getLabelResourceBundle().getString("notebookname"));
        pane.add(label, 0, 0);

        name  = new TextField();
        name.setPromptText(I18NHelper.getLabelResourceBundle().getString("notebookname"));
        if (currentName!=null) name.setText(currentName);
        name.setPrefWidth(getWidth() - label.getLayoutBounds().getWidth() - 5);
        widthProperty().addListener((observable, oldValue, newValue) -> {
                name.setPrefWidth(getWidth()-label.getLayoutBounds().getWidth()-5);
            }
        );
        pane.add(name, 1, 0);

        Button confirm = new Button("OK");
        pane.add(confirm,1,1);
        confirm.setOnAction(event -> {
            this.close();
            final String name = getName();
            Platform.runLater(() -> {
                if (name != null && !"".equals(name)) {
                    try {
                        if (currentUUID==null) Controller.addNoteBook(name);
                        else {
                            Controller.renameNoteBook(currentUUID,name);
                        }
                    } catch (IOException e) {
                        new Alert(parent, "unable to create note book " + e.getMessage()).show();
                    }
                }
            });
        });
    }

    public String getName() {
        return name.getText();
    }

    private TextField name;
    private String  currentUUID;
}
