package org.concordion.integration.junit5;

import org.concordion.integration.TestFrameworkProvider;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

public class JUnit5FrameworkProvider implements TestFrameworkProvider {
    @Override
    public boolean isConcordionFixture(Class<?> clazz) {
        RunWith annotation = clazz.getAnnotation(RunWith.class);
        return annotation != null && ConcordionRunner.class.isAssignableFrom(annotation.value());
    }
}
