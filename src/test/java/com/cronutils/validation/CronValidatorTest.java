package com.cronutils.validation;

import com.cronutils.model.CronType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CronValidatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronValidatorTest.class);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final String expression;
    private final boolean valid;

    public CronValidatorTest(String expression, boolean valid) {
        this.expression = expression;
        this.valid = valid;
    }

    @Parameterized.Parameters(name = "{0} ")
    public static Object[] expressions() {
        return new Object[][]{
                {"0 0 * * * *", true},
                {"*/10 * * * * *", true},
                {"0 0 8-10 * * *", true},
                {"0 0 6,19 * * *", true},
                {"0 0/30 8-10 * * *", true},
                {"0 0 9-17 * * MON-FRI", true},
                {"0 0 0 25 12 ?", true},
                {"0 0 0 L 12 ?", false},
                {"1,2, * * * * *", false},
                {"1- * * * * *", false},
                // Verification for RCE security vulnerability fix: https://github.com/jmrozanec/cron-utils/issues/461
                {"java.lang.Runtime.getRuntime().exec('touch /tmp/pwned'); // 4 5 [${''.getClass().forName('javax.script.ScriptEngineManager').newInstance().getEngineByName('js').eval(validatedValue)}]", false}
        };
    }

    @Test
    public void validateExamples() {
        TestPojo testPojo = new TestPojo(expression);
        Set<ConstraintViolation<TestPojo>> violations = validator.validate(testPojo);
        violations.stream().map(ConstraintViolation::getMessage).forEach(LOGGER::info);

        if (valid) {
            assertTrue(violations.isEmpty());
        } else {
            assertFalse(violations.isEmpty());
        }
    }

    public static class TestPojo {
        @Cron(type = CronType.SPRING)
        private final String cron;

        public TestPojo(String cron) {
            this.cron = cron;
        }

        public String getCron() {
            return cron;
        }

    }
}