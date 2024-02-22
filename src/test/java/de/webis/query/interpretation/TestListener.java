package de.webis.query.interpretation;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
public class TestListener extends RunListener {
    @Override
    public void testRunFinished(Result result) {
        ExplicitEntityLinker.getInstance().close();
    }
}
