package com.cronutils.model.definition;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Issue343Test {

    @Parameterized.Parameters(name = "{0}")
    public static Object[] expressions() {
        // List of expressions from https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
        return new Object[]{
                "0 0 * * * *",
                "*/10 * * * * *",
                "0 0 8-10 * * *",
                "0 0 6,19 * * *",
                "0 0/30 8-10 * * *",
                "0 0 9-17 * * MON-FRI",
                "0 0 0 25 12 ?"
        };
    }

    private final String expression;

    public Issue343Test(String expression) {
        this.expression = expression;
    }

    @Test
    public void testSpringCronExpressions() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

        try {
            Cron parsed = parser.parse(expression);
            Assert.assertNotNull(parsed);
        } catch (IllegalArgumentException e) {
            Assert.fail("This expression should pass: " + expression);
        }
    }
}
