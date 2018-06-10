package org.concordion.integration.junit5;

import org.concordion.Concordion;
import org.concordion.PartialMatches1Test;
import org.concordion.api.Fixture;
import org.concordion.api.ResultSummary;
import org.concordion.api.SpecificationLocator;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.internal.*;
import org.concordion.internal.cache.RunResultsCache;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConcordionTestEngine implements TestEngine {
    public static final String ENGINE_ID = "concordion-classic";
    public static final String ENGINE_DESCRIPTION = "Concordion Classic";
    private final RunResultsCache runResultsCache = RunResultsCache.SINGLETON;


    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        UniqueId uniqueId1 = UniqueId.forEngine(ENGINE_ID);
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId1, ENGINE_DESCRIPTION);
        ConcordionRunner runner = null;

        try {
            runner = new ConcordionRunner(PartialMatches1Test.class);
        } catch (InitializationError initializationError) {
            initializationError.printStackTrace();
        }
        uniqueId1 = uniqueId1.append("runner", "concordion");
        RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(uniqueId1, PartialMatches1Test.class, runner);
        uniqueId1 = uniqueId1.append("test", "concordion");
        runnerTestDescriptor.addChild(new VintageTestDescriptor(uniqueId1.append("test", "%5BConcordion Specification for 'PartialMatches'%5D(spec.examples.PartialMatches1Test"), Description.createTestDescription(PartialMatches1Test.class, "[Concordion Specification for 'PartialMatches'](spec.examples.PartialMatchesTest)")));
        engineDescriptor.addChild(runnerTestDescriptor);
        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        request.getEngineExecutionListener().executionStarted(request.getRootTestDescriptor());

        String example = "example22";
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

        request.getEngineExecutionListener().executionFinished(request.getRootTestDescriptor(), TestExecutionResult.successful());
    }

//    @Override
//    protected EngineExecutionContext createExecutionContext(ExecutionRequest request) {
//        return new EngineExecutionContext() {
//        };
//    }

    protected Fixture createFixture(Object fixtureObject) {
        return new FixtureInstance(fixtureObject);
    }

    protected SpecificationLocator getSpecificationLocator() {
        return new ClassNameAndTypeBasedSpecificationLocator();
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("org.concordion");
    }
}
