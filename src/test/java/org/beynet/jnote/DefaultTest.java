package org.beynet.jnote;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by beynet on 06/04/2015.
 */
public class DefaultTest {
    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Test
    public void T() {
        String test = "rgb(8,64,128)";
        Pattern p = Pattern.compile("rgb\\((\\d+),(\\d+),(\\d+)\\)");
        Matcher matcher = p.matcher(test);
        assertThat(matcher.matches(),is(true));
        System.out.println(matcher.group(1)+" "+matcher.group(2)+" "+matcher.group(3));
    }

}
