package org.beynet.jnote.gui.dialogs;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.jnote.gui.Styles;

/**
 * Created with IntelliJ IDEA.
 * User: beynet
 * Date: 13/10/13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class Alert extends DialogModal {
    public Alert(Stage parent,String message) {
        super(parent,Double.valueOf(150),Double.valueOf(50));
        Text forSize = new Text(message);
        forSize.getStyleClass().add(Styles.ALERT);
        forSize.getStyleClass().addAll(Styles.MESSAGE);
        Button confirm = new Button("ok");

        GridPane grid = new GridPane();
        grid.prefWidthProperty().bind(getCurrentScene().widthProperty());
        //grid.setPadding(new Insets(5));
        grid.setHgap(5);
        grid.setVgap(5);



        getRootGroup().getChildren().add(grid);

        grid.add(forSize,1,0,1,1);
        GridPane.setHalignment(forSize,HPos.CENTER);

        grid.add(confirm,1,1,1,1);
        GridPane.setHalignment(confirm,HPos.CENTER);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(25);
        grid.getColumnConstraints().addAll(c1,c2,c3);


        // resize window to fit the text
        // ------------------------------
        setWidth(forSize.getLayoutBounds().getWidth()/0.5);

        setOnCloseRequest(windowEvent -> windowEvent.consume());

        confirm.setOnMouseClicked(mouseEvent -> close());
    }
}
