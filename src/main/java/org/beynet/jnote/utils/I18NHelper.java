package org.beynet.jnote.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by beynet on 07/03/15.
 */
public class I18NHelper {
    public static final ResourceBundle getLabelResourceBundle() {
        return labelResourceBundle;
    }

    private static final ResourceBundle labelResourceBundle = ResourceBundle.getBundle("LabelsBundle", Locale.getDefault());
}
