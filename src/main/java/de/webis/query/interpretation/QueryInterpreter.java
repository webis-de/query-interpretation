package de.webis.query.interpretation;

import com.google.common.collect.Sets;
import de.webis.query.interpretation.datastructures.*;
import de.webis.query.interpretation.metrics.EntityCommonness;
import de.webis.query.interpretation.metrics.Metric;
import de.webis.query.interpretation.strategies.AllNGrams;
import de.webis.query.interpretation.utils.StreamSerializer;
import de.webis.query.segmentation.core.Segmentation;
import de.webis.query.segmentation.strategies.StrategyWtBaseline;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class QueryInterpreter {
    private final QuerySegmenter querySegmentation;
    private static ExecutorService executorService;
    private static LuceneIndex luceneIndex;

    private Metric commonness;
    private final PersistentStore<String, double[]> embeddingsStorage;

    private double alpha, beta, gamma;

    private final int numThreads = 8;

    private final static Pattern WIKI_URL_PATTERN = Pattern.compile("http(s)?://en.wikipedia.org/wiki/");

    public QueryInterpreter() {
        luceneIndex = new LuceneIndex("./data/persistent/lucene-entity-index");

        embeddingsStorage = new PersistentStore<>("./data/persistent/embeddings/enwiki_500d_db");
        embeddingsStorage.setSerializer(StreamSerializer.class);
        commonness = EntityCommonness.getInstance();

        querySegmentation = new QuerySegmenter(new StrategyWtBaseline(), 0.66);

        alpha = 1.0;
        beta = 1.0;
        gamma = 1.0;
    }

    public Set<Interpretation> annotate(Query query, Set<Entity> entityAnnotations) {
        Map<Segmentation, Integer> segmentationScores = querySegmentation.getSegmentations(query);

        Map<String, Set<Entity>> mentionEntityAnnoMap = new HashMap<>();
        Map<String, Set<String>> mentionEntityMap = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : luceneIndex.get(query).entrySet()) {
            for (String entity : entry.getValue()) {
                entityAnnotations.add(new Entity(0, query.getText().length() - 1, query.getText(), entity));
            }
        }

        for (Entity entityAnnotation : entityAnnotations) {
            mentionEntityAnnoMap.putIfAbsent(entityAnnotation.getMention(),
                    new TreeSet<>((a1, a2) -> a1.getScore() < a2.getScore() ? 1 : -1));


            String entity = entityAnnotation.getUrl();

            entity = WIKI_URL_PATTERN.matcher(entity).replaceAll("");
            entity = entity.replace("_", " ").toLowerCase();
            entityAnnotation.setScore(commonness.get(entity, entityAnnotation.getMention()));

            if (entityAnnotation.getScore() > 0) {
                mentionEntityAnnoMap.get(entityAnnotation.getMention()).add(entityAnnotation);
            }

        }

        for (Map.Entry<String, Set<Entity>> entry : mentionEntityAnnoMap.entrySet()) {
            mentionEntityMap.putIfAbsent(entry.getKey(), new LinkedHashSet<>());
            int limit = 1;

            for (Entity annotation : entry.getValue()) {
                if (mentionEntityMap.get(entry.getKey()).size() < limit) {
                    mentionEntityMap.get(entry.getKey()).add(annotation.getUrl());
                } else {
                    break;
                }
            }

            mentionEntityMap.get(entry.getKey()).add(entry.getKey());
        }

        Set<Interpretation> interpretations = new LinkedHashSet<>();

        for (Map.Entry<Segmentation, Integer> entry : segmentationScores.entrySet()) {
            Segmentation segmentation = entry.getKey();
            List<Set<String>> interpretationCandidates = new LinkedList<>();


            segmentation.getSegments().forEach(s -> interpretationCandidates.add(
                    mentionEntityMap.getOrDefault(s, new HashSet<>(Collections.singletonList(s)))));

            Queue<List<String>> combinations = new ConcurrentLinkedQueue<>(
                    Sets.cartesianProduct(interpretationCandidates));
            Set<Interpretation> annotations = createAnnotations(combinations, segmentation);
            interpretations.addAll(annotations);
        }

        if (interpretations.isEmpty()) {
            for (Map.Entry<Segmentation, Integer> segmention : segmentationScores.entrySet()) {
                interpretations.add(new Interpretation(segmention.getKey().getSegments()));
            }
        }

        List<Interpretation> sortedInterpretations = new LinkedList<>(interpretations);
        sortedInterpretations.sort(Comparator.comparingDouble(Interpretation::getRelevance).reversed());
        Set<Interpretation> results = new LinkedHashSet<>();
        for(Interpretation annotation: sortedInterpretations){
            if(results.size() < 20){
                results.add(annotation);
            }
        }
        return results;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private Set<Interpretation> createAnnotations(Queue<List<String>> interpretations, Segmentation segmentation) {
        executorService = Executors.newFixedThreadPool(numThreads);
        Set<Interpretation> annotations = new CopyOnWriteArraySet<>();
        LongSummaryStatistics commonnessTime = new LongSummaryStatistics();
        LongSummaryStatistics relatednessTime = new LongSummaryStatistics();
        LongSummaryStatistics contextTime = new LongSummaryStatistics();

        Runnable scoringRunnable = () -> {
            while (!interpretations.isEmpty()) {
                List<String> interpretation = interpretations.poll();

                if (interpretation == null) {
                    break;
                }

                Interpretation annotation = new Interpretation(interpretation);

                long start = System.currentTimeMillis();
                double avgCommonness = getAvgCommonness(annotation, segmentation);
                commonnessTime.accept(System.currentTimeMillis() - start);
                start = System.currentTimeMillis();
                double avgRelatedness = getAvgRelatedness(annotation);
                relatednessTime.accept(System.currentTimeMillis() - start);
                start = System.currentTimeMillis();
                double avgContextScore = getAvgContextScore(annotation);
                contextTime.accept(System.currentTimeMillis() - start);

                annotation.setRelevance(
                        alpha * avgCommonness + beta * avgRelatedness + gamma * avgContextScore);

                if (annotation.getRelevance() > 0.0) {
                    annotations.add(annotation);
                }
            }

        };

        for (int i = 0; i < numThreads; i++) {
            executorService.execute(scoringRunnable);
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            executorService.shutdown();
            e.printStackTrace();
        }

        return annotations;
    }

    private double getAvgCommonness(Interpretation interpretation, Segmentation segmentation) {
        List<String> containedEntities = interpretation.getContainedEntities();
        List<String> segments = segmentation.getSegments();
        DoubleSummaryStatistics commonnessStats = new DoubleSummaryStatistics();

        for (String entity : containedEntities) {
            int indexOfEntity = interpretation.getInterpretation().indexOf(entity);

            entity = WIKI_URL_PATTERN.matcher(entity).replaceAll("");
            entity = entity.replace("_", " ").toLowerCase();

            commonnessStats.accept(commonness.get(entity, segments.get(indexOfEntity)));
        }

        return commonnessStats.getAverage();
    }

    private double getAvgRelatedness(Interpretation interpretation) {
        List<String> containedEntities = interpretation.getContainedEntities();
        DoubleSummaryStatistics relatednessStats = new DoubleSummaryStatistics();

        if(containedEntities.size() <= 1){
            return getAvgContextScore(interpretation);
        }

        for (int i = 0; i < containedEntities.size(); i++) {
            String firstEntity = containedEntities.get(i);
            firstEntity = WIKI_URL_PATTERN.matcher(firstEntity).replaceAll("ENTITY/");

            double[] firstEntityVector = embeddingsStorage.getOrDefault(firstEntity, new double[500]);

            for (int j = 0; j < containedEntities.size(); j++) {
                if (i != j) {
                    String secondEntity = containedEntities.get(j);
                    secondEntity = WIKI_URL_PATTERN.matcher(secondEntity).replaceAll("ENTITY/");

                    double[] secondEntityVector = embeddingsStorage.getOrDefault(secondEntity, new double[500]);

                    relatednessStats.accept(Metric.cosineSimilarity(firstEntityVector, secondEntityVector));
                }
            }
        }

        return relatednessStats.getAverage();
    }

    private double getAvgContextScore(Interpretation interpretation) {
        List<String> entities = interpretation.getContainedEntities();
        DoubleSummaryStatistics contextScoreStats = new DoubleSummaryStatistics();

        if (entities.isEmpty()) {
            return 0.0;
        }

        Set<String> contextWords = interpretation.getContextWords();

        if (contextWords.isEmpty()) {
            return 0.0;
        }

        for (String entity : entities) {
            for (String contextWord : contextWords) {
                entity = WIKI_URL_PATTERN.matcher(entity).replaceAll("ENTITY/");

                double[] entityVector = embeddingsStorage.getOrDefault(entity, new double[500]);
                double[] contextVector = embeddingsStorage.getOrDefault(contextWord, new double[500]);

                contextScoreStats.accept(Metric.cosineSimilarity(entityVector, contextVector));
            }
        }

        return contextScoreStats.getAverage();
    }

    public void close() {
        commonness.close();
        embeddingsStorage.close();
    }

    public void setParameter(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    public static void main(String[] args) {
        QueryInterpreter webisQueryInterpretation = new QueryInterpreter();

        Query query = new Query("new york times square dance");
        Set<Interpretation> interpretations = webisQueryInterpretation.annotate(query,
                ExplicitEntityLinker.getInstance(new AllNGrams()).annotate(query));

        for(final Interpretation interpretation: interpretations){
            System.out.println(interpretation);
        }
        webisQueryInterpretation.shutdown();
        webisQueryInterpretation.close();
    }
}
