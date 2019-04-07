package com.cronutils.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CronValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final String expression;
    private final boolean valid;

    public CronValidatorTest(String expression, boolean valid) {
        this.expression = expression;
        this.valid = valid;
    }

    @Parameterized.Parameters(name = "{0} ")
    public static Object[] expressions() {
        // List of expressions from https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
        return new Object[][]{
                {"0 0 * * * *", true},
                {"*/10 * * * * *", true},
                {"0 0 8-10 * * *", true},
                {"0 0 6,19 * * *", true},
                {"0 0/30 8-10 * * *", true},
                {"0 0 9-17 * * MON-FRI", true},
                {"0 0 0 25 12 ?", true},
                {"1, * * * * *", false}
        };
    }

    @Test
    public void validateExamples() {
        TestPojo testPojo = new TestPojo(expression);
        Set<ConstraintViolation<TestPojo>> violations = validator.validate(testPojo);

        if (valid) {
            assertTrue(violations.isEmpty());
        } else {
            assertFalse(violations.isEmpty());
        }
    }

    public static class TestPojo {
        @Cron
        private final String cron;

        public TestPojo(String cron) {
            this.cron = cron;
        }

        public String getCron() {
            return cron;
        }

    }
}