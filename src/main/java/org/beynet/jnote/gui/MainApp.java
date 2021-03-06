package org.beynet.jnote.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.gui.dialogs.Alert;
import org.beynet.jnote.gui.dialogs.NoteBookName;
import org.beynet.jnote.gui.tabs.MainPanel;
import org.beynet.jnote.model.Model;
import org.beynet.jnote.utils.Configuration;
import org.beynet.jnote.utils.I18NHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainApp extends Application {

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        logger.info("version " + Configuration.getVersion());
        Path userHome = Paths.get((String) System.getProperty("user.home"));
        Configuration.initConfiguration(userHome.resolve(".jnoterc"));
        Configuration config = Configuration.getInstance();
        Optional<Path> storageDirectoryPath = config.getStorageDirectoryPath();
        if (storageDirectoryPath.isPresent()==false) {
            config.setStorageDirectoryPath(userHome.resolve(".jnote"));
            storageDirectoryPath=config.getStorageDirectoryPath();
        }
        storageDirectoryPath.orElseThrow(RuntimeException::new);
        Model.createInstance(storageDirectoryPath.get());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.currentStage = primaryStage;
        createMainScene();
    }

    private void createMainScene() {
        Group group = new Group();

        BorderPane pane = new BorderPane();


        group.getChildren().add(pane);

        currentScene = new Scene(group, 1024, 768);
        currentScene.getStylesheets().add(getClass().getResource("/default.css").toExternalForm());

        addMenuBar(pane);
        addMainPane(pane);
        pane.setBottom(null);
        currentStage.setScene(currentScene);
        currentStage.show();

        currentStage.setOnCloseRequest(event -> {
            exitApplication();
        });

    }

    private void addMainPane(BorderPane pane) {
        //Notes tabs = new Notes();
        //pane.setCenter(tabs);
        mainPanel = new MainPanel(currentStage);
        pane.setCenter(mainPanel);
        mainPanel.prefWidthProperty().bind(currentScene.widthProperty());
        mainPanel.setPrefHeight(currentScene.getHeight() - bar.getHeight());
        currentScene.heightProperty().addListener((observable, oldValue, newValue) -> {
            mainPanel.setPrefHeight(currentScene.getHeight() - bar.getHeight());
        });
    }

    private void addMenuBar(BorderPane pane) {
        final ResourceBundle labelResourceBundle = I18NHelper.getLabelResourceBundle();
        bar = new MenuBar();
        bar.setUseSystemMenuBar(true);
        bar.prefWidthProperty().bind(currentScene.widthProperty());
        pane.setTop(bar);

        final Menu file = new Menu(labelResourceBundle.getString("file"));

        // add note book menu item
        // -----------------------
        {
            final MenuItem addNoteBook = new MenuItem(labelResourceBundle.getString("addNoteBook"));
            addNoteBook.setOnAction((evt) -> {
                addNewNoteBook();
            });
            file.getItems().add(addNoteBook);
        }

        // add note book menu item
        // -----------------------
        {
            final MenuItem renameNoteBook = new MenuItem(labelResourceBundle.getString("renameNoteBook"));
            renameNoteBook.setOnAction((evt) -> {
                renameNoteBook();
            });
            file.getItems().add(renameNoteBook);
        }


        // delete current notebook
        // ------------------------
        {
            final MenuItem deleteNoteBook = new MenuItem(labelResourceBundle.getString("deleteNoteBook"));
            deleteNoteBook.setOnAction((evt) -> {
                deleteNoteBook();
            });
            file.getItems().add(deleteNoteBook);
        }

        // reindex all notes
        // -----------------------
        {
            final MenuItem reindexAll = new MenuItem(labelResourceBundle.getString("reindexall"));
            reindexAll.setOnAction((evt) -> {
                try {
                    Controller.reIndexAllNotes();
                } catch (IOException e) {
                    logger.error("error indexing",e);
                    Platform.runLater(()->new Alert(currentStage,"unable to index :"+e.getMessage(),e).show());
                }
            });
            file.getItems().add(reindexAll);
        }

        // about menu item
        // --------------
        {
            String v;
            try {
                v = Configuration.getVersion();
            } catch (IOException e) {
                v="unknown";
            }
            final String version = v;
            final MenuItem about = new MenuItem(labelResourceBundle.getString("about"));
            about.setOnAction((evt) -> {
                new Alert(currentStage,"jnote version="+version).show();
            });
            file.getItems().add(about);
        }

        // exit menu item
        // --------------
        {
            final MenuItem exit = new MenuItem(labelResourceBundle.getString("exit"));
            exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
            exit.setOnAction((evt) -> {
                exitApplication();
            });
            file.getItems().add(exit);
        }




        // add all menu
        // ------------
        bar.getMenus().add(file);


    }

    private void addNewNoteBook() {
        NoteBookName noteBookNameDialog = new NoteBookName(currentStage, Double.valueOf(300), Double.valueOf(60));
        noteBookNameDialog.show();
    }

    private void renameNoteBook() {
        NoteBookName noteBookNameDialog = new NoteBookName(mainPanel.getSelectedNoteBookUUID(),mainPanel.getSelectedNoteBookName(),currentStage, Double.valueOf(300), Double.valueOf(60));
        noteBookNameDialog.show();
    }

    private void deleteNoteBook() {
        try {
            Controller.delNoteBook(mainPanel.getSelectedNoteBookUUID());
        } catch (IOException e) {
            String message = I18NHelper.getLabelResourceBundle().getString("errorDeletingNoteBook")+" : "+e.getMessage();
            logger.error(message,e);
            new Alert(currentStage,message,e).show();
        }
    }

    private void exitApplication() {
        logger.debug("on exit");
        Controller.onExit();
        Platform.exit();
    }

    private Stage currentStage ;
    private Scene currentScene ;
    private MenuBar bar;
    private MainPanel mainPanel;

    private final static Logger logger = Logger.getLogger(MainApp.class);
}
