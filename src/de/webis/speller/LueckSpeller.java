package de.webis.speller;

import de.webis.api.MicrosoftAPIRequestor;
import de.webis.utils.ValueComparator;
import dk.dren.hunspell.Hunspell;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Implementation of the spelling algorithm for queries presented by
 * Gord Lueck on the Microsoft Speller Challenge in 2011.
 */
public class LueckSpeller extends Speller {
    private Hunspell.Dictionary dictionary;
    private MicrosoftAPIRequestor requestor;
    private final Integer numberSuggestions = 2;
    private final Integer errorRate = 36;

    private String SPELLER_TAG = "lueck";

    /**
     * Class constructor of <code>LueckSpeller</code>. The dictionary will be initialized
     * with the american english version of hunspell.
     */
    public LueckSpeller(){
        try {
            dictionary = Hunspell.getInstance().getDictionary("/usr/share/hunspell/en_US");
            requestor = new MicrosoftAPIRequestor();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Class constructor of <code>LueckSpeller</code> specifying a custom hunspell dictionary.
     * @param dictionaryFile path to a *.dic file of a hunspell dictionary.
     */
    public LueckSpeller(String dictionaryFile){
        try {
            dictionary = Hunspell.getInstance().getDictionary(dictionaryFile);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to identify the spelling algorithm for comparing purposes.
     * @return string tag for identification
     */
    @Override
    public String getSpellTag(){
        return SPELLER_TAG;
    }

    /**
     * Gets possible corrections for a given query.
     * @param query string of a query to correct
     * @return      a map of possible corrections for the query with their
     *              calculated normalized confidences
     */
    @Override
    public Map<String, Double> spell(String query){
        List<List<String>> candidates = new ArrayList<>();
        List<String> corrections = new ArrayList<>();

        generateCandidates(query, candidates);
        aggregateCandidates(candidates, corrections, 0, "", query.split(" ").length);

        if(corrections.size() == 1)
            splitPhrases(corrections, query);

        Map<String, Double> scores = normalize(score(corrections, query));

        if(scores.entrySet().iterator().next().getKey().equals(query)){
            scores.clear();
            scores.put(query, 1.0);

            return scores;
        }

        TreeMap<String, Double> sortedScores = new TreeMap<String, Double>(new ValueComparator(scores));
        sortedScores.putAll(scores);

        return new LinkedHashMap<>(sortedScores);
    }

    /**
     * Flushes gathered data to disk.
     */
    @Override
    public void flush() {
        close();
    }

    /**
     * Flushes data to disk and closes the logs.
     * Needs to get called before closing application to keep data in consistent state!
     */
    @Override
    public void close() {
        requestor.close();
    }

    private void splitPhrases(List<String> corrections, String query){
        corrections.addAll(
                requestor.getWordBreakCandidates(
                        query.replaceAll(" ",""), numberSuggestions)
        );
    }

    private void generateCandidates(String query, List<List<String>> candidates){
        String[] terms = query.split("[ ]+");
        List<String> suggestions;

        for(String term: terms){
            if(dictionary.misspelled(term)){
                suggestions = dictionary.suggest(term);

                if(suggestions.size() > this.numberSuggestions){
                    suggestions = new ArrayList<>(suggestions.subList(0, this.numberSuggestions));
                }
                else{
                    suggestions = new ArrayList<>(suggestions);
                }

                suggestions.add(term);

                candidates.add(suggestions);
            }
            else{
                candidates.add(Collections.singletonList(term));
            }
        }
    }

    private Map<String, Double> score(List<String> corrections, String query){
        Map<String, Double> scores = new HashMap<>();
        Double jointProbability;
        Integer levenshteinDistance;
        Double logarithmicProbability;

        Double maxProbability = Double.MIN_VALUE;

        for(String correction: corrections){
            jointProbability = requestor.getJointProbability(correction);

            levenshteinDistance = StringUtils.getLevenshteinDistance(query, correction);
            logarithmicProbability = jointProbability - (errorRate * levenshteinDistance) / (query.length());

            if(Math.exp(logarithmicProbability) > maxProbability){
                maxProbability = Math.exp(logarithmicProbability);
            }

            scores.putIfAbsent(correction, logarithmicProbability);
        }

        for(Map.Entry<String, Double> entry: scores.entrySet()){
            if(Math.exp(entry.getValue()) * 10.0 < maxProbability){
                entry.setValue(Math.pow(10, -15));
            }
        }

        return scores;
    }

    public static void main(String[] args) {
        Speller speller = new LueckSpeller();

        final String query = "examlpe of spealling";
        Map<String, Double> querySpellings = speller.spell(query);

        System.out.println("Possible spellings of \""+query+"\":");
        System.out.println("-----------------------------------");
        for(Map.Entry<String, Double> entry: querySpellings.entrySet()){
            System.out.println(entry.getKey() + " | "+(entry.getValue() * 100.00) + "%");
        }

        speller.close();
    }
}
