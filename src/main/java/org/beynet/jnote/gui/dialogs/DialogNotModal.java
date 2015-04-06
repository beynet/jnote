/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.beynet.jnote.gui.dialogs;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author beynet
 */
public abstract class DialogNotModal extends Dialog {
    public DialogNotModal(Stage parent,Double with,Double height) {
        super(parent, with, height);
        initModality(Modality.NONE);
    }
}
