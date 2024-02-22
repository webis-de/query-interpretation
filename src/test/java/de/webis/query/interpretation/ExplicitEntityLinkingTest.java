package de.webis.query.interpretation;

import de.webis.query.interpretation.datastructures.Entity;
import de.webis.query.interpretation.datastructures.Query;
import de.webis.query.interpretation.strategies.All1Grams;
import de.webis.query.interpretation.strategies.AllNGrams;
import de.webis.query.interpretation.strategies.TokenizationStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
public class ExplicitEntityLinkingTest {
    private ExplicitEntityLinker entityLinker;

    @Before
    public void initialize(){
        try {
            entityLinker = ExplicitEntityLinker.getInstance();
        } catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLinking(){
        final Query testQuery = new Query("new york times square dance");
        final List<TokenizationStrategy> strategyList = Arrays.asList(
                new AllNGrams(),
                new All1Grams()
        );

        try{
            for(final TokenizationStrategy strategy: strategyList){
                entityLinker.setStrategy(strategy);
                final Set<Entity> entitySet = entityLinker.annotate(testQuery);

                int rank = 1;
                System.out.println(strategy.getClass().getSimpleName());
                System.out.printf("%4s | %4s | %4s | %30s | %100s | %5s%n", "RANK", "BEG.", "END", "MENTION", "ENTITY", "SCORE");
                for(final Entity entity: entitySet){
                    System.out.printf("%4d | %s%n", rank, entity.toString());
                    rank++;
                }
            }

        } catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }
}
