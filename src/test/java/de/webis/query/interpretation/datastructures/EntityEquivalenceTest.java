package de.webis.query.interpretation.datastructures;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
public class EntityEquivalenceTest {
    @Test
    public void testEquivalence(){
        Entity entityEncoded = new Entity();
        entityEncoded.setUrl("http://en.wikipedia.org/wiki/Fritz_M%C3%B6ller");


        Entity entityDecoded = new Entity();
        entityDecoded.setUrl("https://en.wikipedia.org/wiki/Fritz_Möller");

        Assert.assertEquals(entityDecoded, entityEncoded);
    }
}
