package de.webis.query.interpretation.strategies;

import de.webis.query.interpretation.datastructures.Query;
import de.webis.query.interpretation.tokenizer.NGramTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AllNGrams implements TokenizationStrategy {
    private final NGramTokenizer nGramTokenizer;

    public AllNGrams() {
        nGramTokenizer = new NGramTokenizer();
    }

    @Override
    public Set<String> apply(Query query) {
        Set<String> results = new HashSet<>();

        TokenStream tokenStream = nGramTokenizer.getSegments(query.getText());
        try {
            while (tokenStream.incrementToken()) {
                results.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
            }

            tokenStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return results;
    }
}
