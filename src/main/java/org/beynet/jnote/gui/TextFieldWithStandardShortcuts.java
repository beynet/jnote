package org.beynet.jnote.gui;


import javafx.scene.control.TextField;

public class TextFieldWithStandardShortcuts extends TextField {
    public TextFieldWithStandardShortcuts(String text) {
        super(text);
        init();
    }

    public TextFieldWithStandardShortcuts() {
        super();
        init();
    }

    void init() {
        setOnKeyReleased(event->{
            if ("a".equalsIgnoreCase(event.getText()) && event.isShortcutDown()) {
                selectAll();
            }
        });
    }
}
