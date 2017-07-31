package de.webis.parser;

import de.webis.datastructures.CorpusCorrection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser class for a given corpus.
 * Implement methods of this abstract class to migrate a corpus
 * into this framework.
 */
public abstract class CorpusParser {
    private String corpusPath;
    private String errorAnnotationPath;

    /**
     * Class constructor specifying the location of a corpus and
     * the location of a file containing its annotated errors
     * @param corpusPath            location of corpus
     * @param errorAnnotationPath   location of error annotation file
     */
    public CorpusParser(String corpusPath, String errorAnnotationPath){
        this.corpusPath = corpusPath;
        this.errorAnnotationPath = errorAnnotationPath;
    }

    /**
     * Get location of represented corpus
     * @return  location of corpus
     */
    public String getCorpusPath(){
        return corpusPath;
    }

    /**
     * Get location of file containing error annotations for the represented corpus
     * @return  location of error annotation file
     */
    public String getErrorAnnotationPath() { return errorAnnotationPath; }

    /**
     * Parse represented corpus
     * @return  list of corrections from the corpus
     */
    public List<CorpusCorrection> parse() {
        try {
            BufferedReader readerCorpus = new BufferedReader(new FileReader(corpusPath));
            BufferedReader readerError = new BufferedReader(new FileReader(errorAnnotationPath));

            List<CorpusCorrection> corrections = new ArrayList<>();
            String lineCorpus, lineError = readerError.readLine();

            boolean after = false;

            while ((lineCorpus = readerCorpus.readLine()) != null) {
                CorpusCorrection correction = getCorrection(lineCorpus);

                while (true) {
                    if (!after) {
                        lineError = readerError.readLine();
                    }

                    if (lineError == null) {
                        break;
                    }

                    if (!lineError.split(";")[0].equals(correction.getQuery())) {
                        after = true;
                        break;
                    }

                    correction.addErrorAnnotations(ErrorAnnotationParser.getExistingErrorTypes(lineError));
                    after = false;
                }

                corrections.add(correction);
            }

            return corrections;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Get an identifier for the represented corpus
     * @return  corpus identifier
     */
    public abstract String getCorpusTag();

    /**
     * Parse a single line from the corpus
     * @param line  line from corpus
     * @return      CorpusCorrection object for the given line
     */
    protected abstract CorpusCorrection getCorrection(String line);
}
