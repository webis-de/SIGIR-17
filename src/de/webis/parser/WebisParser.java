package de.webis.parser;

import de.webis.datastructures.CorpusCorrection;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Parser responsible for webis query speller corpus available under TODO: include link
 */
public class WebisParser extends CorpusParser {
    /**
     * Class constructor (init with default file paths)
     */
    public WebisParser(){
        super("./data/corpora/webis-qspell-17/corpus-webis-qspell-17.csv",
                "./data/corpora/webis-qspell-17/corpus-webis-qspell-17-error-annotations.csv");
    }

    /**
     * Class constructor specifying locations of corpus and its error annotation file
     * @param corpusPath            location of corpus
     * @param errorAnnotationPath   location of error annotation file
     */
    public WebisParser(String corpusPath, String errorAnnotationPath){
        super(corpusPath, errorAnnotationPath);
    }

    /**
     * Get an correction object for a single line of the corpus.
     * @param line  line from corpus
     * @return      correction object
     */
    @Override
    public CorpusCorrection getCorrection(String line) {
        Set<String> sugggestions = new LinkedHashSet<>();
        String[] split = line.split(";");

        for(int i = 2; i < split.length && i < 9; i++){
            if(!split[i].isEmpty())
                sugggestions.add(split[i]);
        }


        return new CorpusCorrection(split[0], split[1], sugggestions);
    }

    /**
     * Get corpus identifier
     * @return  corpus identifier
     */
    @Override
    public String getCorpusTag() {
        return "webis-query-speller-corpus";
    }


}
