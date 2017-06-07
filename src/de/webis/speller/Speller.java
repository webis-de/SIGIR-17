package de.webis.speller;

import java.util.List;
import java.util.Map;

/**
 * An adaptable base class for spell algorithms.
 */
public abstract class Speller {
    public abstract String getSpellTag();

    public abstract Map<String, Double> spell(String query);

    /**
     * Builds all possible combinations of corrections for each word of a query.
     * @param candidates possible corrections for each word of a query such that
     *                   its index matches the position of this word in a query
     * @param results    list of resulting combinations
     * @param depth      parameter for recursion, initialize with 0
     * @param current    parameter for recursion, initialize with empty string
     * @param length     wanted number of words of the resulting combinations
     */
    protected void aggregateCandidates(List<List<String>> candidates, List<String> results, int depth, String current, int length){
        if(depth == candidates.size()){
            if(current.trim().split(" ").length >= length)
                results.add(current.trim().toLowerCase());
            return;
        }

        for(int i=0; i < candidates.get(depth).size(); ++i){
            aggregateCandidates(candidates, results, depth +1, current +" " +candidates.get(depth).get(i), length);
        }
    }

    /**
     * Normalizes the given confidences that they add up to 1.
     * @param confidences   map of corrections and their confidences to normalize
     * @return              normalized map of corrections and their confidences
     */
    protected Map<String, Double> normalize(Map<String, Double> confidences){
        Double sum = 0.0;

        for(Map.Entry<String, Double> entry: confidences.entrySet()){
            sum += entry.getValue();
        }

        for(Map.Entry<String, Double> entry: confidences.entrySet()){
            entry.setValue(entry.getValue() / sum);
        }

        return confidences;
    }

    public abstract void flush();

    public abstract void close();
}
