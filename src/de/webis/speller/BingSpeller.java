package de.webis.speller;

import de.webis.api.MicrosoftAPIRequestor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BingSpeller extends Speller {
    private MicrosoftAPIRequestor requestor;

    public BingSpeller(){
        requestor = new MicrosoftAPIRequestor();
    }

    @Override
    public Map<String, Double> spell(String query) {
        List<String> results = new ArrayList<>();
        List<List<String>> candidates = requestor.spell(query);

        Map<String, Double> spellings = new HashMap<>();

        aggregateCandidates(candidates, results, 0, "", candidates.size());

        for(String result: results){
            spellings.put(result, 1.0 / results.size());
        }

        normalize(spellings);

        return spellings;
    }

    @Override
    public String getSpellTag() {
        return "bing";
    }

    @Override
    public void flush() {
        close();
    }

    @Override
    public void close() {
        requestor.close();
    }

    public static void main(String[] args) {
        Speller speller = new BingSpeller();

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
