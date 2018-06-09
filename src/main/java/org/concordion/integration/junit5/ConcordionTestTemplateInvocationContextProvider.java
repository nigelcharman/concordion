package org.concordion.integration.junit5;

import org.concordion.Concordion;
import org.concordion.PartialMatches1Test;
import org.concordion.api.Fixture;
import org.concordion.api.SpecificationLocator;
import org.concordion.integration.junit4.ConcordionFrameworkMethod;
import org.concordion.internal.ClassNameAndTypeBasedSpecificationLocator;
import org.concordion.internal.FixtureInstance;
import org.concordion.internal.FixtureRunner;
import org.concordion.internal.UnableToBuildConcordionException;
import org.junit.jupiter.api.extension.*;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcordionTestTemplateInvocationContextProvider implements TestTemplateInvocationContextProvider {
    private static AtomicInteger suiteDepth = new AtomicInteger();


    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Class<?> fixtureClass = PartialMatches1Test.class;
        Fixture setupFixture;
        try {
            setupFixture = createFixture(fixtureClass.getConstructor().newInstance());
            // needs to be called so extensions have access to scoped variables
        } catch (Exception e) {
            // TOOD sort out exception handling
            throw new RuntimeException(new InitializationError(e));
        }

        if (suiteDepth.getAndIncrement() == 0) {
            setupFixture.beforeSuite();
        }

        FixtureRunner fixtureRunner;
        try {
            fixtureRunner = new FixtureRunner(setupFixture, getSpecificationLocator());
        } catch (UnableToBuildConcordionException e) {
            // TOOD sort out exception handling
            throw new RuntimeException(new InitializationError(e));
        }
        Concordion concordion = fixtureRunner.getConcordion();

        try {
            concordion.checkValidStatus(setupFixture);

            List<String> examples = concordion.getExampleNames(setupFixture);

            verifyUniqueExampleMethods(examples);

            return examples.stream().map(it -> invocationContext(it));

        } catch (IOException e) {
            // TOOD sort out exception handling
            throw new RuntimeException(new InitializationError(e));
        }
//        return Stream.of(invocationContext("foo"));
    }

    private TestTemplateInvocationContext invocationContext(String parameter) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return parameter;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new ParameterResolver() {
                    @Override
                    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                        return parameterContext.getParameter().getType().equals(String.class);
                    }

                    @Override
                    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                        return parameter;
                    }
                });
            }
        };
    }



    private void verifyUniqueExampleMethods(List<String> exampleNames) {
        // use a hash set to store examples - gives us quick lookup and add.
        Set<String> setOfExamples = new HashSet<String>();

        for (String example: exampleNames) {
            int questionPlace = example.indexOf('?');

            if (questionPlace >=0 ) {
                example = example.substring(0, questionPlace);
            }

            if (setOfExamples.contains(example)) {
                // TOOD sort out exception handling
                throw new RuntimeException(new InitializationError("Specification has duplicate example: '" + example + "'"));
            }
            setOfExamples.add(example);
        }
    }

    /**
     *
     * Protected so superclasses can change the Fixture being returned.
     *
     * @param fixtureObject fixture instance
     * @return fixture
     */
    protected Fixture createFixture(Object fixtureObject) {
        return new FixtureInstance(fixtureObject);
    }

    protected SpecificationLocator getSpecificationLocator() {
        return new ClassNameAndTypeBasedSpecificationLocator();
    }

}
