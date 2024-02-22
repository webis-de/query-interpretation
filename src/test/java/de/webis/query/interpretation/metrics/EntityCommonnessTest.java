package de.webis.query.interpretation.metrics;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
public class EntityCommonnessTest {
    @Test
    public void testIndexExists(){
        final String entity = "the new york times";
        final String mention = "the times";
        Metric entityCommonness = null;

        try{
            entityCommonness = EntityCommonness.getInstance();
            double commonness = entityCommonness.get(
                    entity, mention);
            System.out.printf("%s -> %s : %1.4f%n", mention, entity, commonness);
        } catch (Exception e){
            Assert.fail(e.getMessage());
        } finally {
            if(entityCommonness != null){
                entityCommonness.close();
            }

        }
    }
}
