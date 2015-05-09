package org.beynet.jnote.gui.dialogs;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.jnote.gui.Styles;
import org.beynet.jnote.utils.I18NHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: beynet
 * Date: 13/10/13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class Alert extends DialogModal {
    public Alert(Stage parent,String message) {
        this(parent,message,null);
    }
    public Alert(Stage parent,String message,Exception exception) {
        super(parent,Double.valueOf(150),Double.valueOf(50));
        Text textMessage = new Text(message);
        textMessage.getStyleClass().add(Styles.ALERT);
        textMessage.getStyleClass().addAll(Styles.MESSAGE);
        Button confirm = new Button("ok");

        GridPane grid = new GridPane();

        //grid.setPadding(new Insets(5));
        grid.setHgap(5);
        grid.setVgap(5);





        grid.add(textMessage,1,0,1,1);
        GridPane.setHalignment(textMessage,HPos.CENTER);

        if (exception==null) {
            grid.add(confirm, 1, 1, 1, 1);
            GridPane.setHalignment(confirm, HPos.CENTER);
        }
        else {
            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));
            TextArea exceptionContent = new TextArea();
            Text exceptionContentForSize = new Text(writer.toString());
            exceptionContent.setText(writer.toString());
            exceptionContent.setVisible(false);
            grid.add(confirm, 1, 1, 1, 1);
            GridPane.setHalignment(confirm, HPos.CENTER);
            exceptionContent.prefHeightProperty().bind(getCurrentScene().heightProperty().subtract(confirm.heightProperty()).subtract(textMessage.getLayoutBounds().getHeight()).subtract(20));
            Button showException = new Button(">>>");
            showException.setTooltip(new Tooltip(I18NHelper.getLabelResourceBundle().getString("showHideExceptionTooltip")));
            showException.setOnAction(event -> {
                exceptionContent.setVisible(!exceptionContent.isVisible());
                if (exceptionContent.isVisible()) {
                    setWidth(exceptionContentForSize.getLayoutBounds().getWidth());
                    setHeight(getHeight()+exceptionContentForSize.getLayoutBounds().getHeight());
                }
                else {
                    setWidth(textMessage.getLayoutBounds().getWidth()/0.5);
                    setHeight(getHeight() - exceptionContentForSize.getLayoutBounds().getHeight());
                }
            });
            grid.add(showException, 2, 1, 1, 1);
            grid.add(exceptionContent, 0, 2, 3, 1);
        }

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(25);
        grid.getColumnConstraints().addAll(c1,c2,c3);


        // resize window to fit the text
        // ------------------------------
        setWidth(textMessage.getLayoutBounds().getWidth()/0.5);

        setOnCloseRequest(windowEvent -> windowEvent.consume());

        confirm.setOnMouseClicked(mouseEvent -> close());

        getRootGroup().getChildren().add(grid);
        grid.prefWidthProperty().bind(getCurrentScene().widthProperty());
    }
}
