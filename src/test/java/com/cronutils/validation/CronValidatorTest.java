package com.cronutils.validation;

import com.cronutils.model.CronType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CronValidatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronValidatorTest.class);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static Stream<Arguments> expressions() {
        return Stream.of(
                Arguments.of("0 0 * * * *", true),
                Arguments.of("*/10 * * * * *", true),
                Arguments.of("0 0 8-10 * * *", true),
                Arguments.of("0 0 6,19 * * *", true),
                Arguments.of("0 0/30 8-10 * * *", true),
                Arguments.of("0 0 9-17 * * MON-FRI", true),
                Arguments.of("0 0 0 25 12 ?", true),
                Arguments.of("0 0 0 L 12 ?", false),
                Arguments.of("1,2, * * * * *", false),
                Arguments.of("1- * * * * *", false),
                // Verification for RCE security vulnerability fix: https://github.com/jmrozanec/cron-utils/issues/461
                Arguments.of("java.lang.Runtime.getRuntime().exec('touch /tmp/pwned'); // 4 5 [${''.getClass().forName('javax.script.ScriptEngineManager').newInstance().getEngineByName('js').eval(validatedValue)}]", false)
        );
    }

    @ParameterizedTest(name = "{0} ")
    @MethodSource("expressions")
    public void validateExamples(String expression, boolean valid) {
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
