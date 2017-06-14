package de.webis.datastructures;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A datastructure storing an entry of a speller corpus containing a query its ground truth
 * and optionally annotated error types.
 */
public class CorpusCorrection {
    private String query;
    private Set<String> groundTruth;
    private Set<String> errorAnnotations;

    /**
     * Class constructor specifying query and its ground truth of an entry of a corpus.
     * @param query         query from corpus
     * @param groundTruth   spelling variants for the query of the corpus
     */
    public CorpusCorrection(String query, Set<String> groundTruth){
        this.query = query;
        this.groundTruth = groundTruth;
        errorAnnotations = new HashSet<>();
    }

    /**
     * Class constructor specifying query and its ground truth of an entry of a corpus.
     * Additionally error types are specified.
     * @param query             query from corpus
     * @param groundTruth       spelling variants for the query of the corpus
     * @param errorAnnotations  types of errors contained in the query with respect to the ground truth
     */
    public CorpusCorrection(String query, Set<String> groundTruth, Set<String> errorAnnotations){
        this.query = query;
        this.groundTruth = groundTruth;
        this.errorAnnotations = errorAnnotations;
    }

    /**
     * Copy constructor.
     * @param correction correction to copy
     */
    public CorpusCorrection(CorpusCorrection correction){
        query = correction.getQuery();
        groundTruth = new LinkedHashSet<>(correction.getGroundTruth());
        errorAnnotations = new LinkedHashSet<>(correction.errorAnnotations);
    }

    /**
     * Append an error type annotation.
     * @param errorAnnotations set containing error types to add
     */
    public void addErrorAnnotations(Set<String> errorAnnotations){
        if(this.errorAnnotations == null){
            errorAnnotations = new HashSet<>();
        }

        this.errorAnnotations.addAll(errorAnnotations);
    }

    /**
     * Check whether an error type appears for this corpus entry.
     * @param error name of error type
     * @return      true if error type is contained for this corpus entry
     */
    public boolean hasErrorAnnotation(String error){
        return errorAnnotations.contains(error);
    }

    /**
     * Add an entry to the ground truth
     * @param groundTruthEntry entry to add
     */
    public void addToGroundTruth(String groundTruthEntry){
        groundTruth.add(groundTruthEntry);
    }

    /**
     * Check whether the ground truth does not contain the query
     * @return true if the query is not contained in the ground truth
     */
    public boolean containsErrorLowerBound(){
        return !groundTruth.contains(query);
    }

    /**
     * Check whether the ground truth contains an alternation which is not the query itself
     * @return true if there are other alternations for the query besides the query itself
     */
    public boolean containsErrorUpperBound(){
        if(groundTruth.contains(query))
            for(String suggestion: groundTruth){
                if(!suggestion.equals(query)){
                    return true;
                }
            }

        return false;
    }

    /**
     * Check whether there is an upper bound or a lower bound error for this corpus entry
     * @return true if there is an error
     */
    public boolean containsError(){
        return containsErrorLowerBound() || containsErrorUpperBound();
    }

    /**
     * Get the query for this corpus entry.
     * @return query
     */
    public String getQuery(){
        return query;
    }

    /**
     * Get set of ground truth entry for the query
     * @return ground truth
     */
    public Set<String> getGroundTruth(){
        return groundTruth;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(query).append("\t");

        for(String suggestion: groundTruth){
            builder.append(suggestion).append("\t");
        }

        return builder.toString().trim();
    }

    @Override
    public boolean equals(Object correction){
        if(correction instanceof CorpusCorrection){
            if(!((CorpusCorrection) correction).query.equals(this.query)){
                return false;
            }

            if(((CorpusCorrection) correction).groundTruth.size() != this.groundTruth.size()){
                return false;
            }

            Iterator<String> rhsIterator = ((CorpusCorrection) correction).groundTruth.iterator();

            for (String suggestion : this.groundTruth) {
                if (!suggestion.equals(rhsIterator.next())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
