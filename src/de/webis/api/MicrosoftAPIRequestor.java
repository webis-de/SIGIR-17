package de.webis.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.webis.datastructures.MultiValueLogger;
import de.webis.datastructures.SingleValueLogger;
import de.webis.exceptions.InvalidSubscriptionException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class retrieves information from several language analysis APIs from Microsoft.
 * All information gathered will be saved on disk. To query results not already contained
 * in the logs, you have to put a valid API subscription key in <code>/resources/subscription.properties</code>
 */
public class MicrosoftAPIRequestor {

    private HttpClient httpClient;
    private URI uri;
    private Properties subscriptionKeys;

    private static SingleValueLogger jointProbabilityLog;
    private static MultiValueLogger wordBreakCandidatesLog;
    private static MultiValueLogger spellCandidatesLog;

    /**
     * Class constructor
     */
    public MicrosoftAPIRequestor(){
        httpClient = new DefaultHttpClient();

        try {
            BufferedInputStream stream = new BufferedInputStream(
                    new FileInputStream("./resources/subscription.properties"));

            subscriptionKeys = new Properties();
            subscriptionKeys.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the probability with that the given word-based n-gram occurs
     * together in the web. Information gets retrieved by
     * <a href="https://azure.microsoft.com/en-us/services/cognitive-services/web-language-model/">Microsoft Web-Language-Model API</a>.
     *
     * @param  query word-based n-gram
     * @return       logarithmic probability for the given query
     */
    public double getJointProbability(String query){
        if(jointProbabilityLog == null){
            jointProbabilityLog = new SingleValueLogger("./data/log/joint-probability/");
        }

        if(jointProbabilityLog.contains(query)){
            return jointProbabilityLog.get(query);
        }

        Double prob = 0.0;

        StringBuilder builder = new StringBuilder();
        builder.append("{ \"queries\": [ ");
        builder.append("\"").append(query).append("\"");
        builder.append("]}");

        try {
            prob = getJointProbability(new StringEntity(builder.toString()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        jointProbabilityLog.log(query, prob);

        return prob;
    }

    /**
     * Gets the probability with that the given word-based n-gram occurs
     * together in the web. Information gets retrieved by
     * <a href="https://azure.microsoft.com/en-us/services/cognitive-services/web-language-model/">Microsoft Web-Language-Model API</a>.
     *
     * @param  requestBody json request body of query
     * @return             logarithmic probability for the given query
     */
    private double getJointProbability(StringEntity requestBody){
        try {
            URIBuilder builder;
            builder = new URIBuilder("https://api.projectoxford.ai/text/weblm/v1.0/calculateJointProbability");
            builder.setParameter("model", "query");
            builder.setParameter("order", "5");

            uri = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Double probability = 0.0;

        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKeys.getProperty("weblm-api-subscription-key"));

        request.setEntity(requestBody);

        HttpResponse response;
        try {
            response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String results = EntityUtils.toString(entity);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readValue(results, JsonNode.class);

                if(node.has("results")){
                    node = node.get("results");
                    Iterator<JsonNode> iter = node.elements();

                    if(iter.hasNext()){
                        probability = iter.next().get("probability").asDouble();
                    }
                }
                else{
                    close();
                    throw new InvalidSubscriptionException(
                            node.get("error").get("message").toString()
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return probability;
    }


    /**
     * Gets possible separations of one word queries. Information gets retrieved by
     * <a href="https://azure.microsoft.com/en-us/services/cognitive-services/web-language-model/">
     *     Microsoft Web-Language-Model API</a>.
     * @param query         query without whitespaces
     * @param maxCandidates number of max. suggestions returned
     * @return              list of possible separations of the given query
     */
    public List<String> getWordBreakCandidates(String query, int maxCandidates){
        if(wordBreakCandidatesLog == null){
            wordBreakCandidatesLog = new MultiValueLogger("./data/log/word-break-candidates/");
        }

        if(wordBreakCandidatesLog.contains(query)){
            return wordBreakCandidatesLog.get(query);
        }

        try {
            URIBuilder builder;
            builder = new URIBuilder("https://api.projectoxford.ai/text/weblm/v1.0/breakIntoWords");
            builder.setParameter("model", "query");
            builder.setParameter("text",query.replaceAll("[ ]+",""));
            builder.setParameter("order", "5");
            builder.setParameter("maxNumOfCandidatesReturned", String.valueOf(maxCandidates));

            uri = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        List<String> candidates = new ArrayList<>();

        HttpUriRequest request = new HttpPost(uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKeys.getProperty("weblm-api-subscription-key"));
        HttpResponse response;
        try {
            response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String results = EntityUtils.toString(entity);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readValue(results, JsonNode.class);

                if(node.has("candidates")){
                    node = node.get("candidates");

                    Iterator<JsonNode> iter = node.elements();

                    while(iter.hasNext()){
                        candidates.add(iter.next().get("words").textValue());
                    }
                }
                else{
                    close();
                    throw new InvalidSubscriptionException(
                            node.get("error").get("message").toString()
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        wordBreakCandidatesLog.log(query, candidates);

        return candidates;

    }

    /**
     * Gets spell corrections for each term of a query based on
     * <a href="https://azure.microsoft.com/en-us/services/cognitive-services/spell-check/">
     *     Bing Spell Check API</a>
     * @param query query to check spelling
     * @return      list of several suggestions for each word in the given query
     */
    public List<List<String>> spell(String query){
        if(spellCandidatesLog == null){
            spellCandidatesLog = new MultiValueLogger("./data/log/bing-spellings/");
        }

        List<List<String>> candidates = new ArrayList<>();
        List<String> words = Arrays.asList(query.split("[ ]+"));
        Integer numFilled = 0;

        for(int i = 0 ; i < words.size(); i++){
            candidates.add(new ArrayList<>());

            if(spellCandidatesLog.contains(words.get(i))){
                candidates.get(i).addAll(new HashSet<>(spellCandidatesLog.get(words.get(i))));
            }

            if(!candidates.get(i).isEmpty()){
               numFilled++;
            }
        }

        if(numFilled == words.size()){
            return candidates;
        }

        try {
            URIBuilder builder;
            builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/spellcheck/");
            builder.setParameter("mode", "spell");

            uri = builder.build();

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setHeader("Ocp-Apim-Subscription-Key",subscriptionKeys.getProperty("spellcheck-api-subscription-key"));

            StringEntity entity = new StringEntity("text="+query.replaceAll("[ ]+", "+"));
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            String result = EntityUtils.toString(response.getEntity());


            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(result, JsonNode.class);

            if(node.has("flaggedTokens")){
                node = node.get("flaggedTokens");
                Iterator<JsonNode> iterTokens = node.elements();
                while (iterTokens.hasNext()){
                    JsonNode tokenNode = iterTokens.next();
                    Iterator<JsonNode> iterSuggestions = tokenNode.get("suggestions").elements();
                    List<String> suggestions = new ArrayList<>();

                    while(iterSuggestions.hasNext()){
                        JsonNode suggestion = iterSuggestions.next();
                        suggestions.add(suggestion.get("suggestion").asText());
                    }
                    String tokenString = tokenNode.get("token").asText();
                    int index = words.indexOf(tokenString.split("[ ]+")[0]);

                    if(index == -1){
                        for(int i = 0; i < words.size(); i++){
                            if(words.get(i).contains(tokenString)){
                                index = i;
                                break;
                            }
                        }
                    }

                    candidates.set(index, suggestions);

                    for(int i = index + 1; i < tokenString.split("[ ]+").length; i++){
                        candidates.set(i, Collections.singletonList(""));
                    }


                    spellCandidatesLog.log(tokenString, suggestions);
                }
            }
            else{
                close();
                if(node.has("message")){
                    throw new InvalidSubscriptionException(
                            node.get("message").toString()
                    );
                }else{
                    throw new RuntimeException(node.toString());
                }

            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < candidates.size(); i++){
            if(candidates.get(i).isEmpty()){
                candidates.get(i).add(words.get(i));
                spellCandidatesLog.log(words.get(i), words.get(i));
            }
        }

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return candidates;
    }

    /**
     * All logged results gathered from Microsofts APIs gets flushed to disk.
     * Needs to be called before closing application to keep the data in consistent state!
     */
    public void close(){
        if(jointProbabilityLog != null){
            jointProbabilityLog.close();
            jointProbabilityLog = null;
        }


        if(wordBreakCandidatesLog != null){
            wordBreakCandidatesLog.close();
            wordBreakCandidatesLog = null;
        }


        if(spellCandidatesLog != null){
            spellCandidatesLog.close();
            spellCandidatesLog = null;
        }

    }
}