package de.webis.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class handling error type annotations.
 */
public class ErrorAnnotationParser {
    private static final int spaceError = 3;
    private static final int characterError = 4;
    private static final int insertionError = 5;
    private static final int deletionError = 6;
    private static final int substitutionError = 7;
    private static final int transpositionError = 8;

    /**
     * Get the contained error types for a single line from the error annotation file
     * @param annotationLine    line from error annotation file
     * @return                  set of contained error types
     */
    public static Set<String> getExistingErrorTypes(String annotationLine){
        Set<String> errors = new HashSet<>();
        String[] split = annotationLine.split(";");

        if(Integer.valueOf(split[spaceError]) > 0){
            errors.add("space");
        }

        if(Integer.valueOf(split[characterError]) > 0){
            errors.add("character");
        }

        if(Integer.valueOf(split[insertionError]) > 0){
            errors.add("insertion");
        }

        if(Integer.valueOf(split[deletionError]) > 0){
            errors.add("deletion");
        }

        if(Integer.valueOf(split[substitutionError]) > 0){
            errors.add("substitution");
        }

        if(Integer.valueOf(split[transpositionError]) > 0){
            errors.add("transposition");
        }

        return errors;
    }

    /**
     * Count contained error types for all queries (once per query) from the error annotation file
     * @param errorAnnotationPath   location of error annotation file
     * @return                      mapping of error type and number of affected queries
     */
    public static Map<String, Double> getErrorTypeDistributionPerQuery(String errorAnnotationPath){
        Map<String, Double> errorTypeFrequencies = new HashMap<>();
        Set<String> currentAnnotations = new HashSet<>();

        String query = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(errorAnnotationPath));
            // skip header
            String line = reader.readLine();

            while((line = reader.readLine()) != null){
                if(!query.equals(line.split(";")[0])){
                    for(String annotation: currentAnnotations){
                        errorTypeFrequencies.put(annotation,
                                errorTypeFrequencies.getOrDefault(annotation, 0.0) + 1.0);
                    }

                    query = line.split(";")[0];
                    currentAnnotations.clear();
                }

                currentAnnotations.addAll(getExistingErrorTypes(line));

            }

            for(String annotation: currentAnnotations){
                errorTypeFrequencies.put(annotation,
                        errorTypeFrequencies.getOrDefault(annotation, 0.0) + 1.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorTypeFrequencies;
    }

    /**
     * Count contained error types for all spelling variants of all
     * queries from the error annotation file
     * @param errorAnnotationPath   location of error annotation file
     * @return                      mapping of error type and number of appearance
     */
    public static Map<String, Double> getErrorTypeDistributionPerSpelling(String errorAnnotationPath){
        Map<String, Double> errorTypeFrequencies = new HashMap<>();

        errorTypeFrequencies.put("space", 0.0);
        errorTypeFrequencies.put("character", 0.0);
        errorTypeFrequencies.put("deletion", 0.0);
        errorTypeFrequencies.put("insertion", 0.0);
        errorTypeFrequencies.put("substitution", 0.0);
        errorTypeFrequencies.put("transposition", 0.0);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(errorAnnotationPath));
            String line = reader.readLine();
            Map<String, Double> numErrorAnnotations = new HashMap<>();

            while((line = reader.readLine()) != null){
                numErrorAnnotations = countErrorTypes(line);

                for(Map.Entry<String, Double> entry: numErrorAnnotations.entrySet()){
                    errorTypeFrequencies.put(entry.getKey(),
                            errorTypeFrequencies.get(entry.getKey()) + entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorTypeFrequencies;
    }

    private static Map<String, Double> countErrorTypes(String annotationLine){
        Map<String, Double> errorAnnotations = new HashMap<>();
        String[] split = annotationLine.split(";");

        errorAnnotations.put("space",
                errorAnnotations.getOrDefault("space", 0.0) + Double.valueOf(split[spaceError]));
        errorAnnotations.put("character",
                errorAnnotations.getOrDefault("character", 0.0) + Double.valueOf(split[characterError]));
        errorAnnotations.put("deletion",
                errorAnnotations.getOrDefault("deletion", 0.0) + Double.valueOf(split[deletionError]));
        errorAnnotations.put("insertion",
                errorAnnotations.getOrDefault("insertion", 0.0) + Double.valueOf(split[insertionError]));
        errorAnnotations.put("substitution",
                errorAnnotations.getOrDefault("substitution", 0.0) + Double.valueOf(split[substitutionError]));
        errorAnnotations.put("transposition",
                errorAnnotations.getOrDefault("transposition", 0.0) + Double.valueOf(split[transpositionError]));

        return errorAnnotations;
    }
}
