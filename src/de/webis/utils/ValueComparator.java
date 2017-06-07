package de.webis.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator used for a <code>TreeMap</code> to sort by its values.
 */
public class ValueComparator implements Comparator{
    private Map<String, Double> map;

    /**
     * Class constructor specifying the input map.
     * @param input map to sort
     */
    public ValueComparator(Map input) {
        map = input;
    }

    /**
     * Compare numerical size of two values from the input map with respect to their keys.
     * @param key1 key to compare value for
     * @param key2 key to compare value for
     * @return -1 if value for key1 is bigger of equal as value for key2
     *          1 otherwise
     */
    public int compare(Object key1, Object key2){
        if(map.get(key1)>=map.get(key2)){
            return -1;
        }
        else{
            return 1;
        }
    }

}