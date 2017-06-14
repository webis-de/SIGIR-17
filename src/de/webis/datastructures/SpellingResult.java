package de.webis.datastructures;

import de.webis.utils.ValueComparator;

import java.util.*;

/**
 * A datastructure representing the result of a given spell algorithm for
 * an entry of a given corpus.
 */
public class SpellingResult {
    private String query;
    private Set<String> groundTruth;

    private Map<String, Double> rankedResults;

    /**
     * Class constructor.
     * @param query          query from corpus
     * @param groundTruth    ground truth for the given query
     * @param rankedResults  results from a spell algorithm for the given query and its confidences
     */
    public SpellingResult(String query,
                          Set<String> groundTruth,
                          Map<String, Double> rankedResults){
        this.query = query;
        this.groundTruth = groundTruth;
        this.rankedResults = rankedResults;

        sortByConfidence();
    }


    /**
     * Get stored spelling results for the stored query without its confidences.
     * @return  set of the spelled query
     */
    public Set<String> getResultSet(){
        return rankedResults.keySet();
    }

    /**
     * Get stored ground truth for the query from the corpus
     * @return  ground truth for query
     */
    public Set<String> getGroundTruth(){
        return groundTruth;
    }

    /**
     * Get confidence for one of the stored spelling results
     * @param result    spelling result
     * @return          confidence of the specified spelling result or
     *                  null if there is no such result
     */
    public Double getConfidence(String result){
        if(rankedResults.containsKey(result))
            return rankedResults.get(result);
        else{
            System.out.println("ERROR: No prob found for \""+result+"\"");
        }

        return null;
    }

    /**
     * Check whether the ground truth contains the given spelling results
     * @param spelling  spelling result
     * @return          true if the ground truth contains the spelling
     */
    public boolean inGroundTruth(String spelling){
        return groundTruth.contains(spelling);
    }

    /**
     * Check whether the spelled results contains a desirable spelling.
     * @param desirable desirable spelling
     * @return          true if the results contain the given spelling
     */
    public boolean isSpelledAlternative(String desirable){
        return rankedResults.containsKey(desirable);
    }

    /**
     * Check whether the query got spelled correctly with respect to its ground truth
     * @return  true if at least one spelling result equals an entry of the ground truth
     */
    public boolean isCorrect(){
        for(Map.Entry<String, Double> entry: rankedResults.entrySet()){
            if(groundTruth.contains(entry.getKey())){
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate EF1 values for a list of spelling results
     * @param results list of results of a spell algorithm
     * @return        EP, ER and EF1
     */
    public static EF1 getEF1(List<SpellingResult> results){
        Double sumEP = 0.0;
        Double sumER = 0.0;

        for(SpellingResult result: results){
            for(String alternative: result.getResultSet()){
                if(result.inGroundTruth(alternative)){
                    sumEP += result.getConfidence(alternative);
                }
            }

            for(String alternation: result.getGroundTruth()){
                if(result.isSpelledAlternative(alternation)){
                    sumER += 1 / result.getGroundTruth().size();
                }
            }
        }

        Double EP = (1.0 / results.size()) * sumEP;
        Double ER = (1.0 / results.size()) * sumER;

        return new EF1(EP, ER, (2.0*EP*ER)/(ER+EP));
    }

    /**
     * Calculate Precision@1 for a list of spelling results
     * @param results list of results of a spell algorithm
     * @return        Precision@1
     */
    public static Double getPrecision(List<SpellingResult> results){
        Double precision = 0.0;

        for(SpellingResult result: results){
            if(result.rankedResults.size() > 0){
                Double firstValue = result.rankedResults.entrySet().iterator().next().getValue();

                for(Map.Entry<String, Double> entry: result.rankedResults.entrySet()){
                    if(!entry.getValue().equals(firstValue)){
                        break;
                    }

                    if(result.inGroundTruth(entry.getKey())){
                        precision += 1.0;
                        break;
                    }
                }
            }

        }

        return precision / (double)(results.size());
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(query);

        Iterator i = rankedResults.keySet().iterator();

        while (i.hasNext()){
            builder.append(", ");

            builder.append(i.next());
        }

        builder.append(", ground truth: ");

        for(String alternation: groundTruth){
            builder.append(", ").append(alternation);
        }

        return builder.toString();
    }

    private void sortByConfidence(){
        Map<String, Double> sorted = new TreeMap<String, Double>(new ValueComparator(rankedResults));
        sorted.putAll(rankedResults);
        rankedResults = new LinkedHashMap<>(rankedResults);
    }
}
