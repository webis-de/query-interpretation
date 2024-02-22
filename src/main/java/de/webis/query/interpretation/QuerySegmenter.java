package de.webis.query.interpretation;

import de.webis.query.interpretation.datastructures.Query;
import de.webis.query.segmentation.application.QuerySegmentation;
import de.webis.query.segmentation.core.Segmentation;
import de.webis.query.segmentation.strategies.SegmentationStrategy;
import de.webis.query.segmentation.strategies.StrategyWikiBased;
import de.webis.query.segmentation.strategies.StrategyWtBaseline;

import java.util.Map;

public class QuerySegmenter {
    private final SegmentationStrategy segmentationStrategy;
    private final SegmentationStrategy fallbackStrategy;
    private final double scoreDiffThreshold;

    public QuerySegmenter() {
        segmentationStrategy = new StrategyWtBaseline();
        fallbackStrategy = new StrategyWikiBased();
        scoreDiffThreshold = 0.7;
    }

    public QuerySegmenter(SegmentationStrategy segmentationStrategy) {
        this.segmentationStrategy = segmentationStrategy;
        fallbackStrategy = new StrategyWikiBased();
        scoreDiffThreshold = 0.7;
    }

    public QuerySegmenter(SegmentationStrategy segmentationStrategy, double scoreDiffThreshold) {
        this.segmentationStrategy = segmentationStrategy;
        fallbackStrategy = new StrategyWikiBased();
        this.scoreDiffThreshold = scoreDiffThreshold;
    }

    public Map<Segmentation, Integer> getSegmentations(Query query) {
        Map<Segmentation, Integer> segmentationScores;

        QuerySegmentation querySegmentation = new QuerySegmentation(segmentationStrategy);
        Object[] segmentations = querySegmentation.performSegmentationWithFilteration(
                new de.webis.query.segmentation.core.Query(query.getText()),
                scoreDiffThreshold
        );

        segmentationScores = querySegmentation.performSegmentationWithHeuristic(segmentations);
        boolean zeroScores = segmentationScores.entrySet().stream().allMatch(e -> e.getValue() == 0);

        if (zeroScores) {
            segmentationScores.clear();

            querySegmentation = new QuerySegmentation(fallbackStrategy);
            segmentations = querySegmentation.performSegmentationWithFilteration(
                    new de.webis.query.segmentation.core.Query(query.getText()),
                    scoreDiffThreshold
            );

            segmentationScores = querySegmentation.performSegmentationWithHeuristic(segmentations);
        }

        return segmentationScores;
    }

    public static void main(String[] args) {
        QuerySegmenter querySegmentation = new QuerySegmenter(new StrategyWikiBased(), 0.7);
        System.out.println(querySegmentation.getSegmentations(new Query("new york times square dance")));
    }
}
