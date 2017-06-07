package de.webis.datastructures;

import io.multimap.Iterator;
import io.multimap.Map;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a persistent 1:n key-value store.
 * It builds an interface for <a href="http://multimap.io/">Multimap</a>
 */
public class MultiValueLogger {
    private static Map log;

    /**
     * Class constructor specifying save location.
     * @param pathToLog path to folder where persistent data gets saved
     */
    public MultiValueLogger(String pathToLog) {
        File dir = new File(pathToLog);

        if(!dir.exists()){
            dir.mkdirs();
        }


        if(!new File(pathToLog+"multimap.map.id").exists()){
            Map.Options options = new Map.Options();
            options.setCreateIfMissing(true);
            try {
                log = new Map(pathToLog, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                log = new Map(pathToLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if a given key exists.
     * @param key key to check
     * @return    true if key exists and retrieved values aren't empty strings
     */
    public boolean contains(String key){
        Iterator iter = log.get(key);

        if(iter.hasNext()){
            if(new String(iter.nextAsByteArray()).isEmpty()){
                iter.close();
                return false;
            }
        }
        else{
            iter.close();
            return false;
        }

        iter.close();
        return true;
    }

    /**
     * Checks if a given key-value pair exists.
     * @param key    key to check
     * @param value  value for the given key to check
     * @return       true if the given key-value pair exists
     */
    public boolean contains(String key, String value){
        Iterator iter = log.get(key);

        while(iter.hasNext()){
            if(new String(iter.nextAsByteArray()).equals(value)){
                iter.close();
                return true;
            }
        }

        iter.close();
        return false;
    }

    /**
     * Inserts the given key-value pair into the map or appends the value
     * to the list of stored values with respect to the key if the key already exists.
     * @param key   key in map
     * @param entry entry to insert/append with respect to the key
     */
    public void log(String key, String entry) {
        try {
            if(log != null && !key.isEmpty()){
                log.put(key, entry.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends the list of entries to the stored with respect to the key or inserts
     * them all if the key don't exists.
     * @param key     key in map
     * @param entries entries to insert/append with respect to the key
     */
    public void log(String key, List<String> entries){
        for(String entry: entries){
            log(key, entry);
        }
    }

    /**
     * Retrieves list of values for the given key.
     * @param key key to get values for
     * @return    list of stored values or an empty list for non-existing keys
     */
    public List<String> get(String key){
        Iterator iter = log.get(key);
        List<String> results = new ArrayList<>((int)iter.available());

        while (iter.hasNext()){
            try {
                String desResult = new String(iter.nextAsByteArray());
                results.add(desResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        iter.close();

        return results;
    }

    /**
     * Replaces a value with respect to a given key with a new value.
     * @param key      key in map
     * @param oldValue value to replace with respect to the key
     * @param newValue new value to insert with respect to the key
     */
    public void replace(String key, String oldValue, String newValue){
        log.replaceFirstEqual(key, oldValue.getBytes(), newValue.getBytes());
    }

    /**
     * Removes a key and all of its entries.
     * @param key key to remove
     */
    public void remove(String key){
        log.remove(key);
    }

    /**
     * Gets the total numbers of keys in the map
     * @return number of keys
     */
    public Long size(){
        return log.getStats().getNumKeysTotal();
    }

    /**
     * Flushes data to disk and closes the log.
     * Needs to get called before closing application to keep data in consistent state!
     */
    public void close(){
        log.close();
    }
}
