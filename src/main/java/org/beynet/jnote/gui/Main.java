package org.beynet.jnote.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.jnote.controler.Controller;
import org.beynet.jnote.gui.tabs.MainPanel;
import org.beynet.jnote.model.Model;
import org.beynet.jnote.utils.I18NHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Main extends Application {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        Path userHome = Paths.get((String) System.getProperty("user.home"));
        Model.createInstance(userHome.resolve(".jnote"));
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
        currentStage.setScene(currentScene);
        currentStage.show();
    }

    private void addMainPane(BorderPane pane) {
        //Notes tabs = new Notes();
        //pane.setCenter(tabs);
        pane.setCenter(new MainPanel());
    }

    private void addMenuBar(BorderPane pane) {
        final ResourceBundle labelResourceBundle = I18NHelper.getLabelResourceBundle();
        final MenuBar bar = new MenuBar();
        bar.prefWidthProperty().bind(currentStage.widthProperty());
        pane.setTop(bar);

        final Menu file = new Menu(labelResourceBundle.getString("file"));
        final MenuItem exit = new MenuItem(labelResourceBundle.getString("exit"));
        exit.setOnAction((evt)->{
            exitApplication();
        });
        file.getItems().add(exit);

        // add all menu
        // ------------
        bar.getMenus().add(file);


    }

    private void exitApplication() {
        logger.debug("on exit");
        Controller.onExit();
        System.exit(0);
    }

    private Stage currentStage ;
    private Scene currentScene ;

    private final static Logger logger = Logger.getLogger(Main.class);
}
