package org.beynet.jnote.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by beynet on 01/05/2015.
 */
public class Configuration {

    public static void initConfiguration(Path configurationFilePath) throws IOException {
        _configuration = new Configuration(configurationFilePath);
    }
    public static Configuration getInstance() {
        return _configuration;
    }

    private Configuration(Path configurationFilePath) throws IOException {
        this.configurationFilePath=configurationFilePath;
        this.properties = new Properties();
        if (Files.exists(this.configurationFilePath)) {
            try (InputStream is = Files.newInputStream(this.configurationFilePath)) {
                this.properties.load(is);
            }
        }
    }

    /**
     * @return the storage path found in the actual property file
     */
    public synchronized Optional<Path> getStorageDirectoryPath() {
        Optional<Path> result = Optional.empty();
        Object o = this.properties.get(STORAGE_DIRECTORY_PATH);
        if (o !=null && o instanceof String) {
            try {
                Path p = Paths.get((String) o);
                if (p!=null) result=Optional.of(p);
            } catch(InvalidPathException e) {
                logger.error("invalid path "+o,e);
            }
        }
        return result;
    }

    /**
     * change the storage path
     * @param storageDirectoryPath
     */
    public synchronized void setStorageDirectoryPath(Path storageDirectoryPath) {
        if (storageDirectoryPath==null) throw new IllegalArgumentException("storage directory path must not be null");
        this.properties.put(STORAGE_DIRECTORY_PATH,storageDirectoryPath.toString());
        save();
    }

    public synchronized String getPreferredFont() {
        String result = "Comic Sans MS";
        Object o = this.properties.get(PREFERRED_FONT);
        if (o !=null && o instanceof String) {
            result=(String)o;
        }
        else {
            setPreferredFont(result);
        }
        return result;
    }
    public synchronized void setPreferredFont(String font) {
        this.properties.put(PREFERRED_FONT,font);
        save();
    }

    public synchronized String getPreferredColor() {
        String result = "rgb(8,64,128)";
        Object o = this.properties.get(PREFERRED_COLOR);
        if (o !=null && o instanceof String) {
            result=(String)o;
        }
        else {
            setPreferredColor(result);
        }
        return result;
    }
    public synchronized void setPreferredColor(String font) {
        this.properties.put(PREFERRED_COLOR,font);
        save();
    }


    public synchronized List<String> getFontList() {
        List<String> result = new ArrayList<>();
        String fonts = (String) this.properties.get(FONTS);
        if (fonts==null) {
            fonts ="Arial,Comic Sans MS,Courier New";
            this.properties.put(FONTS,fonts);
            save();
        }
        String[] tokens = fonts.split(",");
        result.addAll(Arrays.asList(tokens));
        return result;
    }

    private void save() {
        try (OutputStream os = Files.newOutputStream(configurationFilePath)){
            properties.store(os, null);
        } catch (IOException e) {
            logger.error("unable to save property file");
        }
    }


    public static String getVersion() throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Configuration.class.getResourceAsStream("/version.txt"),"UTF-8")) ) {
            return reader.readLine();
        }
    }

    private Path       configurationFilePath;
    private Properties properties;


    private final static String STORAGE_DIRECTORY_PATH = "storageDirectoryPath";
    private final static String PREFERRED_FONT         = "preferredFont";
    private final static String PREFERRED_COLOR        = "preferredColor";
    private final static String FONTS                  = "fonts";
    private static       Configuration _configuration  = null ;
    private final static Logger logger                 = Logger.getLogger(Configuration.class);
}
