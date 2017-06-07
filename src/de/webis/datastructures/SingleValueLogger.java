package de.webis.datastructures;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class represents a persistent 1:1 key-value store.
 * It builds an interface for <a href=http://rocksdb.org/>RocksDB</a>
 */
public class SingleValueLogger {
    private RocksDB log;

    /**
     * Class constructor specifying save location.
     * @param pathToLog path to folder where persistent data gets saved
     */
    public SingleValueLogger(String pathToLog){
        File file = new File(pathToLog+"IDENTITY");

        if(!file.exists()){
            try {
                Runtime.getRuntime().exec("mkdir -p "+pathToLog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            log = RocksDB.open(options, pathToLog);
        }
        catch (RocksDBException e1) {e1.printStackTrace();}
    }

    /**
     * Inserts the given key-value pair into the map if it doesn't exists.
     * @param key   key in map
     * @param value value to insert with respect to the key
     */
    public void log(String key, Double value){
        try {
            if(!contains(key)){
                byte[] bytes = new byte[8];
                ByteBuffer.wrap(bytes).putDouble(value);

                log.put(key.getBytes(), bytes);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves value for the given key.
     * @param key key to get value for
     * @return    value with respect to the key
     */
    public Double get(String key){
        try {
            if(log.get(key.getBytes()) != null){
                ByteBuffer buffer = ByteBuffer.wrap(log.get(key.getBytes()));
                return buffer.getDouble();
            }

        } catch (RocksDBException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Checks if a given key exists.
     * @param key key to check
     * @return    true if key exists and retrieved value isn't 0
     */
    public boolean contains(String key){
        Double val = get(key);
        return !(val == null || val == 0.0);
    }

    /**
     * Flushes data to disk and closes the log.
     * Needs to get called before closing application to keep data in consistent state!
     */
    public void close(){
        log.close();
    }
}
