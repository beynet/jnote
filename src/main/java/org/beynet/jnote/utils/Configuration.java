package org.beynet.jnote.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

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
    public Optional<Path> getStorageDirectoryPath() {
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

    private void save() {
        try (OutputStream os = Files.newOutputStream(configurationFilePath)){
            properties.store(os,null);
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
    private static Configuration _configuration = null ;
    private final static Logger logger = Logger.getLogger(Configuration.class);
}
