package com.cronutils.model.definition;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class Issue343Test {
    @ParameterizedTest
    @ValueSource(strings = {
        "0 0 * * * *",
        "*/10 * * * * *",
        "0 0 8-10 * * *",
        "0 0 6,19 * * *",
        "0 0/30 8-10 * * *",
        "0 0 9-17 * * MON-FRI",
        "0 0 0 25 12 ?"
    })
    public void testSpringCronExpressions(String expression) {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

        try {
            Cron parsed = parser.parse(expression);
            assertNotNull(parsed);
        } catch (IllegalArgumentException e) {
            fail("This expression should pass: " + expression);
        }
    }
}
