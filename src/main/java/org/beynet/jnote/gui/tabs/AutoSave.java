package org.beynet.jnote.gui.tabs;

import javafx.application.Platform;
import org.apache.log4j.Logger;

import java.util.function.Supplier;

/**
 * Created by beynet on 08/06/2015.
 */
public class AutoSave extends Thread {

    public AutoSave(Runnable save,Supplier<String> htmlSupplier,Supplier<Boolean> isDisable) {
        this.save = save;
        this.htmlSupplier=htmlSupplier;
        setSkipNextSave(true);
        this.isDisable=isDisable;
        this.lastHTML=null;
    }

    @Override
    public void run() {
        logger.info("starting autosave");
        while(Thread.currentThread().isInterrupted()==false) {
            if (Boolean.FALSE.equals(isDisable.get()) && isSkipNextSave()==false) {
                String newHTML = htmlSupplier.get();
                if (getLastHtml()!=null && !getLastHtml().equals(newHTML)) {
                    logger.debug("!!!!!! autosaving");
                    setLastHtml(newHTML);
                    save.run();
                }
            }
            setSkipNextSave(false);
            synchronized (this) {
                try {
                    this.wait(3*1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("autosave stopped");
    }

    public synchronized String getLastHtml() {
        return lastHTML;
    }

    public synchronized void setSkipNextSave(boolean skipNextSave) {
        this.skipNextSave = skipNextSave;
        this.notify();
    }

    public synchronized boolean isSkipNextSave() {
        return skipNextSave;
    }

    public synchronized void setLastHtml(String lastHTML) {
        this.lastHTML = lastHTML;
    }

    private String lastHTML;
    private boolean skipNextSave ;
    private final Supplier<String> htmlSupplier;
    private final Supplier<Boolean> isDisable;
    private final Runnable save;
    private final static Logger logger = Logger.getLogger(AutoSave.class);
}
