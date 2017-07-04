package de.webis.speller;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a baseline for spell algorithms by
 * returning a query as its correction with a confidences
 * of 1.
 */
public class BaselineSpeller extends Speller {
    @Override
    public Map<String, Double> spell(String query) {
        Map<String, Double> prob = new HashMap<>();
        prob.put(query, 1.0);
        return prob;
    }

    @Override
    public String getSpellTag() {
        return "baseline";
    }

    @Override
    public void close(){}

    @Override
    public void flush() {}

}
