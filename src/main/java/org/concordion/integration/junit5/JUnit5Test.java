package org.concordion.integration.junit5;

import org.concordion.Concordion;
import org.concordion.PartialMatches1Test;
import org.concordion.api.Fixture;
import org.concordion.api.ResultSummary;
import org.concordion.api.SpecificationLocator;
import org.concordion.internal.*;
import org.concordion.internal.cache.RunResultsCache;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.List;


@ExtendWith(ConcordionTestTemplateInvocationContextProvider.class)
public class JUnit5Test {
    private final RunResultsCache runResultsCache = RunResultsCache.SINGLETON;

    @TestTemplate
    void test(String example) {
        System.out.println(example);
        Class<?> fixtureClass = PartialMatches1Test.class;
        Fixture setupFixture;
        try {
            setupFixture = createFixture(fixtureClass.getConstructor().newInstance());
            // needs to be called so extensions have access to scoped variables
        } catch (Exception e) {
            // TOOD sort out exception handling
            throw new RuntimeException(new InitializationError(e));
        }

        FixtureRunner fixtureRunner;
        try {
            fixtureRunner = new FixtureRunner(setupFixture, getSpecificationLocator());
        } catch (UnableToBuildConcordionException e) {
            // TOOD sort out exception handling
            throw new RuntimeException(new InitializationError(e));
        }

        boolean firstRun = null == runResultsCache.getFromCache(fixtureClass, null);

        Concordion concordion = fixtureRunner.getConcordion();

        try {
            // only setup the fixture if it hasn't been run before
            if (firstRun) {
                runResultsCache.startFixtureRun(setupFixture, concordion.getSpecificationDescription());
                setupFixture.beforeSpecification();
            }

            // create the new fixture because there is a new fixture object.
            Fixture fixture = createFixture(fixtureClass.getConstructor().newInstance());

            try {
                ResultSummary result = fixtureRunner.run(example, fixture);
                result.assertIsSatisfied(fixture);

            } catch (ConcordionAssertionError e) {
                throw e;
            } catch (IOException e) {
                throw e;
            }


            // only actually finish the specification if this is the first time it was run
            if (firstRun) {
                setupFixture.afterSpecification();
                concordion.finish();
            }

            RunOutput results = runResultsCache.getFromCache(fixtureClass, null);

            if (results != null) {
                synchronized (System.out) {
                    results.getActualResultSummary().print(System.out, setupFixture);
                }
            }
        } catch (Exception e) {
//            notifier.fireTestFailure(new Failure(getDescription(), e));
//            throw e;
        } finally {
//            if (suiteDepth.decrementAndGet() == 0) {
//                setupFixture.afterSuite();
//            }
        }
    }

    protected Fixture createFixture(Object fixtureObject) {
        return new FixtureInstance(fixtureObject);
    }

    protected SpecificationLocator getSpecificationLocator() {
        return new ClassNameAndTypeBasedSpecificationLocator();
    }
}
