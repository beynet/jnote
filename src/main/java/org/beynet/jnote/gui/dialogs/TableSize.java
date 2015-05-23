package org.beynet.jnote.gui.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.jnote.utils.I18NHelper;

/**
 * Created by beynet on 22/05/2015.
 */
public class TableSize extends DialogModal {
    public TableSize(Stage parent) {
        super(parent,Double.valueOf(20),Double.valueOf(93));
        Text colsLabel = new Text(I18NHelper.getLabelResourceBundle().getString("col"));
        Text rowsLabel = new Text(I18NHelper.getLabelResourceBundle().getString("row"));
        cols = new TextField();
        cols.setPromptText(I18NHelper.getLabelResourceBundle().getString("nbCols"));
        rows = new TextField();
        rows.setPromptText(I18NHelper.getLabelResourceBundle().getString("nbRows"));
        validated=Boolean.FALSE;
        Button confirm = new Button(I18NHelper.getLabelResourceBundle().getString("insertTable"));
        Text confirmT = new Text(confirm.getText());
        confirm.setOnAction(event -> {
            while(true) {
                try {
                    Integer.valueOf(cols.getText());
                } catch (Exception e) {
                    new Alert(this,I18NHelper.getLabelResourceBundle().getString("expectInteger")).showAndWait();
                    break;
                }
                try {
                    Integer.valueOf(rows.getText());
                } catch (Exception e) {
                    new Alert(this,I18NHelper.getLabelResourceBundle().getString("expectInteger")).showAndWait();
                    break;
                }
                close();
                validated=Boolean.TRUE;
                break;
            }
        });

        GridPane pane = new GridPane();
        pane.prefWidthProperty().bind(getCurrentScene().widthProperty());
        pane.prefHeightProperty().bind(getCurrentScene().heightProperty());
        getRootGroup().getChildren().add(pane);

        ColumnConstraints c1,c2,c3;
        c1=new ColumnConstraints();
        c1.setPrefWidth(Math.max(colsLabel.getLayoutBounds().getWidth(),rowsLabel.getLayoutBounds().getWidth()));
        c2=new ColumnConstraints();
        c2.setPrefWidth(confirmT.getLayoutBounds().getWidth()+40);
        c3=new ColumnConstraints();
        c3.setPrefWidth(10);

        pane.getColumnConstraints().addAll(c1,c2,c3);

        pane.add(colsLabel,0,0);
        pane.add(cols,1,0,2,1);

        pane.add(rowsLabel,0,1);
        pane.add(rows,1,1,2,1);

        pane.add(confirm, 1, 2,1,1);

        cols.prefWidthProperty().bind(pane.prefWidthProperty().subtract(confirmT.getLayoutBounds().getWidth()).subtract(20));
        rows.prefWidthProperty().bind(pane.prefWidthProperty().subtract(confirmT.getLayoutBounds().getWidth()).subtract(20));

        setWidth(c1.getPrefWidth()+c2.getPrefWidth()+c3.getPrefWidth()+20);
    }

    public Long getRowSize() {
        return Long.valueOf(rows.getText());
    }

    public Long getColSize() {
        return Long.valueOf(cols.getText());
    }

    public Boolean isValidated() {
        return validated;
    }

    private TextField rows;
    private TextField cols;
    private Boolean   validated;
}
