package de.webis.query.interpretation;

import de.webis.query.interpretation.datastructures.Entity;
import de.webis.query.interpretation.datastructures.PersistentStore;
import de.webis.query.interpretation.datastructures.Query;
import de.webis.query.interpretation.strategies.AllNGrams;
import de.webis.query.interpretation.strategies.TokenizationStrategy;
import de.webis.query.interpretation.metrics.EntityCommonness;
import de.webis.query.interpretation.metrics.Metric;
import de.webis.query.interpretation.utils.StreamSerializer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ExplicitEntityLinker {
    private final static String INDEX_PATH = "data/persistent/wiki-entity-index";

    private final PersistentStore<String, Set<String>> index;

    private TokenizationStrategy strategy;
    private final Metric entityCommonness;

    private static ExplicitEntityLinker INSTANCE;

    public static ExplicitEntityLinker getInstance(){
        return getInstance(new AllNGrams());
    }
    public static ExplicitEntityLinker getInstance(final TokenizationStrategy strategy){
        if(INSTANCE == null){
            INSTANCE = new ExplicitEntityLinker(strategy);
        }else{
            INSTANCE.setStrategy(strategy);
        }

        return INSTANCE;
    }

    private ExplicitEntityLinker(final TokenizationStrategy strategy) {
        this.strategy = strategy;

        if(!new File(INDEX_PATH).exists()){
            throw new RuntimeException(new FileNotFoundException("Index (" + INDEX_PATH + ") does not exist!"));
        }

        index = new PersistentStore<>(INDEX_PATH);
        index.setSerializer(StreamSerializer.class);

        entityCommonness = EntityCommonness.getInstance();
    }

    public Set<Entity> annotate(Query query) {
        Set<Entity> entityAnnotations = new LinkedHashSet<>();

        Set<String> segments = strategy.apply(query);
        List<String> sortedSegments = new LinkedList<>(segments);
        sortedSegments.sort(Comparator.comparingInt(String::length).reversed());

        for (String segment : sortedSegments) {
            Set<String> annotations = index.get(segment);

            if (annotations != null) {

                for (String annotation : annotations) {
                    Entity entityAnnotation = new Entity();
                    entityAnnotation.setBegin(query.getText().indexOf(segment));
                    entityAnnotation.setEnd(entityAnnotation.getBegin() + segment.length());
                    entityAnnotation.setMention(segment);
                    entityAnnotation.setUrl(annotation);
                    String entityName = FilenameUtils.getBaseName(entityAnnotation.getUrl());
                    entityName = entityName.toLowerCase().replaceAll("_", " ");
                    entityAnnotation.setScore(
                            entityCommonness.get(
                                    entityName,
                                    entityAnnotation.getMention())
                    );

                    if(entityAnnotation.getScore() > 0){
                        entityAnnotations.add(entityAnnotation);
                    }
                }
            }
        }

        List<Entity> sortedAnnotations = new LinkedList<>(entityAnnotations);
        sortedAnnotations.sort(Comparator.comparingDouble(Entity::getScore).reversed());

        return new LinkedHashSet<>(sortedAnnotations);
    }

    public void setStrategy(TokenizationStrategy strategy) {
        this.strategy = strategy;
    }

    public void close() {
        index.close();
        if(entityCommonness != null){
            entityCommonness.close();
        }
    }
}
