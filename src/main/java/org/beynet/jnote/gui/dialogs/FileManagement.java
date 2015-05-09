package org.beynet.jnote.gui.dialogs;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.utils.I18NHelper;

import java.io.File;

/**
 * Created by beynet on 22/04/2015.
 */
public class FileManagement extends DialogModal {
    public FileManagement(Stage parent,AttachmentRef attachmentRef) {
        super(parent, 300d, 100d);
        this.attachmentRef = attachmentRef;
        GridPane pane = new GridPane();



        StringBuilder text = new StringBuilder();
        text.append("file name=");
        text.append(this.attachmentRef.getFileName());
        text.append(" size =");
        text.append(this.attachmentRef.getSize());

        TextArea fileDescription  = new TextArea();
        fileDescription.setEditable(false);
        fileDescription.setText(text.toString());

        Button download = new Button("download");
        download.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File result = directoryChooser.showDialog(this);
            Task<Void> t = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Controller.saveAttachment(attachmentRef,result.toPath());
                        Platform.runLater(() -> new Alert(parent, I18NHelper.getLabelResourceBundle().getString("fileDownloaded")).show());
                    } catch(Exception e) {
                        Platform.runLater(() -> new Alert(parent, I18NHelper.getLabelResourceBundle().getString("errorDownloadingFile") + e.getMessage(),e).show());
                    }
                    return null;
                }
            };
            new Thread(t).start();
            close();
        });

        Button delete = new Button("delete");
        delete.setOnAction(event -> {
            Confirm confirm = new Confirm(this, "do you really want to delete attached file ?");
            confirm.showAndWait();
            if (confirm.isConfirmed()) {
                try {
                    Controller.deleteAttachment(attachmentRef);
                } catch (Exception e) {
                    logger.error("unable to remove attachment", e);
                }
            }
            close();
        });

        //binding width
        fileDescription.setPrefWidth(getWidth());
        widthProperty().addListener((observable, oldValue, newValue) -> {
            fileDescription.setPrefWidth(newValue.doubleValue());
        });

        //binding height
        heightProperty().addListener((observable, oldValue, newValue) -> {
            fileDescription.setPrefHeight(newValue.doubleValue() - delete.getHeight()-30);
        });


        pane.add(fileDescription, 0, 0, 3, 2);
        pane.add(download,0,3);
        pane.add(delete,2,3);

        getRootGroup().getChildren().add(pane);
    }


    private AttachmentRef attachmentRef;

    private final static Logger logger = Logger.getLogger(FileManagement.class);
}
