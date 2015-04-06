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
public abstract class DialogModal extends Dialog {
    public DialogModal(Stage parent,Double with,Double height) {
        super(parent, with, height);
        initModality(Modality.APPLICATION_MODAL);
    }

}
