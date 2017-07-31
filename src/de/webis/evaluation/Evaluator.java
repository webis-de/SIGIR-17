package de.webis.evaluation;

import de.webis.datastructures.SpellingResult;
import de.webis.datastructures.CorpusCorrection;
import de.webis.parser.CorpusParser;
import de.webis.parser.ErrorAnnotationParser;
import de.webis.parser.WebisParser;
import de.webis.speller.BaselineSpeller;
import de.webis.speller.LueckSpeller;
import de.webis.speller.Speller;
import de.webis.utils.MathUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluation class to get statistic measures of a corpora or to
 * determine the quality of a spelling algorithm.
 */
public class Evaluator {
    /**
     * Print EF1 and Precision@1 measures to evaluate a spelling algorithm
     * @param spellAlgorithm    algorithm to evaluate
     * @param parser            parser specifying corpus to evaluate on
     */
    public void evaluateSpeller(Speller spellAlgorithm, CorpusParser parser){
        System.out.println("Evaluate "+spellAlgorithm.getSpellTag()+" algorithm on "+parser.getCorpusTag()+"...");
        List<CorpusCorrection> corpusCorrections = parser.parse();
        List<SpellingResult> spellingResults = new ArrayList<>();

        List<SpellingResult> spellingSpaceError = new ArrayList<>();
        List<SpellingResult> spellingCharacterError = new ArrayList<>();
        List<SpellingResult> spellingInsertionError = new ArrayList<>();
        List<SpellingResult> spellingDeletionError = new ArrayList<>();
        List<SpellingResult> spellingSubstitutionError = new ArrayList<>();
        List<SpellingResult> spellingTranspositionError = new ArrayList<>();

        List<SpellingResult> spellingWithoutError = new ArrayList<>();

        Map<String, Double> spelling;

        for(CorpusCorrection corpusCorrection: corpusCorrections){
            spelling = spellAlgorithm.spell(corpusCorrection.getQuery());
            SpellingResult result = new SpellingResult(corpusCorrection.getQuery(),
                    corpusCorrection.getGroundTruth(),
                    spelling);

            spellingResults.add(result);

            if(corpusCorrection.hasErrorAnnotation("space")){
                spellingSpaceError.add(result);
            }

            if(corpusCorrection.hasErrorAnnotation("character")){
                spellingCharacterError.add(result);
            }

            if(corpusCorrection.hasErrorAnnotation("insertion")){
                spellingInsertionError.add(result);
            }

            if(corpusCorrection.hasErrorAnnotation("deletion")){
                spellingDeletionError.add(result);
            }

            if(corpusCorrection.hasErrorAnnotation("substitution")){
                spellingSubstitutionError.add(result);
            }

            if(corpusCorrection.hasErrorAnnotation("transposition")){
                spellingTranspositionError.add(result);
            }

            if(!corpusCorrection.isDefinitelyMisspelled() && !corpusCorrection.isPotentiallyMisspelled()){
                spellingWithoutError.add(result);
            }

            if(spellingResults.size() % 100 == 0){
                System.out.print("\rProcessed queries: "+spellingResults.size());
            }

            if(spellingResults.size() % 1000 == 0){
                spellAlgorithm.flush();
            }
        }
        System.out.print("\rProcessed queries: "+spellingResults.size());
        System.out.println("\nDone.\n");

        System.out.println("Results:");
        System.out.println("---------------");
        System.out.println("General:");
        System.out.println(SpellingResult.getEF1(spellingResults));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingResults));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("No Error:");
        System.out.println(SpellingResult.getEF1(spellingWithoutError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingWithoutError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Space Error:");
        System.out.println(SpellingResult.getEF1(spellingSpaceError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingSpaceError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Character Error:");
        System.out.println(SpellingResult.getEF1(spellingCharacterError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingCharacterError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Insertion Error:");
        System.out.println(SpellingResult.getEF1(spellingInsertionError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingInsertionError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Deletion Error:");
        System.out.println(SpellingResult.getEF1(spellingDeletionError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingDeletionError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Substitution Error:");
        System.out.println(SpellingResult.getEF1(spellingSubstitutionError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingSubstitutionError));
        System.out.println("---------------");
        System.out.println();

        System.out.println("---------------");
        System.out.println("Transposition Error:");
        System.out.println(SpellingResult.getEF1(spellingTranspositionError));
        System.out.println("Precision@1: "+SpellingResult.getPrecision(spellingTranspositionError));
        System.out.println("---------------");
        System.out.println();

        try {
            writeFile(spellingResults, "./data/output/"+spellAlgorithm.getSpellTag()+"/"
                    +spellAlgorithm.getSpellTag()+"-"+parser.getCorpusTag()+"-spelling.csv");

            PrintWriter writer = new PrintWriter(new FileWriter("./data/output/"+spellAlgorithm.getSpellTag()
                    +"/"+spellAlgorithm.getSpellTag()+"-"+parser.getCorpusTag()+"-ef1.txt"));

            writer.println(SpellingResult.getEF1(spellingResults));
            writer.println("Precision@1: "+SpellingResult.getPrecision(spellingResults));

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        spellAlgorithm.close();
    }

    /**
     * Print out statistical measures of the specified corpus.
     * @param parser    corpus for analysis
     */
    public void analyzeCorpus(CorpusParser parser){
        Integer sumMinLevenshteinDistDefinite = 0;
        Integer sumMinLevenhsteinDist = 0;

        Map<Integer, Integer> levenshteinDistPotential = new HashMap<>();
        Map<Integer, Integer> levenshteinDistDefinite = new HashMap<>();

        List<CorpusCorrection> corpusCorrections = parser.parse();

        List<CorpusCorrection> potentialMisspellings = new ArrayList<>();
        List<CorpusCorrection> definiteMisspellings = new ArrayList<>();

        Integer numSpellingVariantsPotential = 0;
        Integer numSpellingVariantsDefinite = 0;

        for(CorpusCorrection correction: corpusCorrections){
            if(correction.containsError()) {
                if(correction.isPotentiallyMisspelled()){
                    potentialMisspellings.add(correction);
                    numSpellingVariantsPotential += correction.getGroundTruth().size();
                }


                if(correction.isDefinitelyMisspelled()){
                    definiteMisspellings.add(correction);
                    numSpellingVariantsDefinite += correction.getGroundTruth().size();
                }


                Integer minLevenshteinDistDefinite = Integer.MAX_VALUE;
                Integer minLevenshteinDist = Integer.MAX_VALUE;

                for (String desSpellingVariant : correction.getGroundTruth()) {
                    int distance = StringUtils.getLevenshteinDistance(desSpellingVariant, correction.getQuery());

                    if(correction.isPotentiallyMisspelled()){
                        levenshteinDistPotential.put(distance, levenshteinDistPotential.getOrDefault(distance, 0) + 1);
                    }

                    if(correction.isDefinitelyMisspelled()){
                        levenshteinDistDefinite.put(distance, levenshteinDistDefinite.getOrDefault(distance, 0) + 1);

                        if(minLevenshteinDistDefinite > distance && !desSpellingVariant.equals(correction.getQuery())){
                            minLevenshteinDistDefinite = distance;
                        }
                    }

                    if(correction.isDefinitelyMisspelled() || correction.isPotentiallyMisspelled())
                    if(minLevenshteinDist > distance){
                        minLevenshteinDist = distance;
                    }
                }

                if(minLevenshteinDistDefinite != Integer.MAX_VALUE)
                    sumMinLevenshteinDistDefinite += minLevenshteinDistDefinite;

                if(minLevenshteinDist != Integer.MAX_VALUE)
                    sumMinLevenhsteinDist += minLevenshteinDist;
            }

        }

        int numPotentialMisspellings = potentialMisspellings.size();
        int numDefiniteMisspellings = definiteMisspellings.size();
        int numMisspellings = numPotentialMisspellings + numDefiniteMisspellings;

        int corpusSize = corpusCorrections.size();

        double misspellingPercentage = MathUtil.roundDouble((double)(numMisspellings) / (double)(corpusSize) * 100.0, 2);
        double potentialMisspellingPercentage = MathUtil.roundDouble((double)(numPotentialMisspellings) / (double)(corpusSize) * 100.0, 2);
        double definiteMisspellingPercentage = MathUtil.roundDouble((double)(numDefiniteMisspellings) / (double)(corpusSize) * 100.0, 2);

        double avgMinLevenshteinDistDefinite = MathUtil.roundDouble((double)(sumMinLevenshteinDistDefinite) / (double)(numDefiniteMisspellings),2);
        double avgMinLevenshteinDist = MathUtil.roundDouble((double)(sumMinLevenhsteinDist) / (double)(numMisspellings),2);

        double numSpellingVariantsPerQueryPotential = MathUtil.roundDouble((double)(numSpellingVariantsPotential) / (double)(numPotentialMisspellings), 2);
        double numSpellingVariantsPerQueryDefinite = MathUtil.roundDouble((double)(numSpellingVariantsDefinite) / (double)(numDefiniteMisspellings),2);

        Map<String, Double> numQueriesWithErrorType =
                ErrorAnnotationParser.getErrorTypeDistributionPerQuery(
                        parser.getErrorAnnotationPath()
                );

        Map<String, Double> numSpellingsWithErrorType =
                ErrorAnnotationParser.getErrorTypeDistributionPerSpelling(
                        parser.getErrorAnnotationPath()
                );

        System.out.println("Corpus: "+parser.getCorpusTag());
        System.out.println("Size: "+corpusSize+"\n");
        System.out.println("Potential Misspellings: "+numPotentialMisspellings+" ("+potentialMisspellingPercentage+"%)");
        System.out.println("Definite Misspellings:  "+numDefiniteMisspellings+" ("+definiteMisspellingPercentage+"%)");
        System.out.println("Potential + Definite:   "+numMisspellings+" ("+misspellingPercentage+"%)\n");

        System.out.println("Avg. min. Levenshtein distance (definite): "+avgMinLevenshteinDistDefinite);
        System.out.println("Avg. min. Levenshtein distance (potential + definite): "+avgMinLevenshteinDist+"\n");

        System.out.println("Number of spelling variants per query (potential): "+numSpellingVariantsPerQueryPotential);
        System.out.println("Number of spelling variants per query (definite): "+numSpellingVariantsPerQueryDefinite+"\n");

        System.out.println("Levenshtein distance frequencies (potential): \n"+levenshteinDistPotential);
        System.out.println("Levenshtein distance frequencies (definite): \n"+levenshteinDistDefinite);

        System.out.println();
        System.out.println("Number of queries with error:");
        System.out.println(numQueriesWithErrorType);
        numQueriesWithErrorType.replaceAll((key, value) -> MathUtil.roundDouble(value / (double) numMisspellings, 4));
        System.out.println(numQueriesWithErrorType);
        System.out.println();
        System.out.println();
        System.out.println("Number of spelling variants with error:");
        System.out.println(numSpellingsWithErrorType);
        Integer finalNumSpellingVariantsDefinite = numSpellingVariantsDefinite;
        Integer finalNumSpellingVariantsPotential = numSpellingVariantsPotential;
        numSpellingsWithErrorType.replaceAll(
                (key, value) ->
                        MathUtil.roundDouble(
                                value / (double)(finalNumSpellingVariantsDefinite + finalNumSpellingVariantsPotential),4
                        )
        );
        System.out.println(numSpellingsWithErrorType);
        System.out.println();
    }

    private void writeFile(List items, String path) throws IOException {
        boolean succeed = true;

        File file = new File(path);

        if(!file.getParentFile().exists()){
            succeed = file.getParentFile().mkdirs();
        }

        if(!succeed){
            throw new IOException("Can't create parent directories: "+file.getPath());
        }

        PrintWriter writer = new PrintWriter(new FileWriter(file));

        for(Object result: items){
            writer.println(result);
        }

        writer.close();
    }

    public static void main(String[] args) {
        Evaluator evaluator = new Evaluator();

        /* CORPUS ANALYSIS */
        evaluator.analyzeCorpus(new WebisParser());

        /* WEBIS CORPUS - EF1 Evaluation */
        evaluator.evaluateSpeller(new BaselineSpeller(), new WebisParser());
        evaluator.evaluateSpeller(new LueckSpeller(), new WebisParser());
    }

}
