package de.webis.speller;

import de.webis.datastructures.MultiValueLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class GoogleSpeller extends Speller {
    private static MultiValueLogger logger;

    private final double alsoContainsResultsForRating = 0.51;

    private final double didYouMeanRating = 0.25;


    public GoogleSpeller(){
        logger = new MultiValueLogger("./data/log/google-spellings/");
    }

    @Override
    public Map<String, Double> spell(String query) {
        Map<String, Double> probabilities = new HashMap<>();

        if(!logger.contains(query)){
            try {
                Process process = Runtime.getRuntime().exec("phantomjs ./resources/js/google-search-scraper.js "+query+"");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line = "";

                while((line = reader.readLine()) != null){
                    if(!line.isEmpty() && line.contains("|")){
                        String[] splits = line.split("[|]");

                        if(splits[0].contains("enthält auch ergebnisse für")){
                            splits[1] = splits[0].replace("enthält auch ergebnisse für ","").trim();
                            splits[0] = "enthält auch ergebnisse für";
                        }

                        rate(query, splits[1], splits[0], probabilities);
                        logger.log(query, splits[0].trim());
                        logger.log(query, splits[1].trim());
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            rate(query, logger.get(query).get(1), logger.get(query).get(0), probabilities);
        }

        return probabilities;
    }

    private void rate(String query, String spelling, String tag, Map<String, Double> results){
        if(tag.contains("meintest du:")){
            results.put(spelling.trim(), didYouMeanRating);
            results.put(query, 1.0 - didYouMeanRating);
        }
        else if(tag.contains("enthält auch ergebnisse für")){
            results.put(spelling.trim(), alsoContainsResultsForRating);
            results.put(query, 1.0 - alsoContainsResultsForRating);
        }
        else{
            results.put(spelling.trim(), 1.0);
        }
    }

    @Override
    public String getSpellTag() {
        return "google";
    }

    @Override
    public void flush() {
        logger.close();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger = new MultiValueLogger("./data/log/google-spellings/");
    }

    @Override
    public void close() {
        logger.close();
    }

    public static void main(String[] args) {
        Speller speller = new GoogleSpeller();

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
