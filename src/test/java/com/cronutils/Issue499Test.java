package com.cronutils;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue499Test {
    /**
     * We want to convert Unix cron expressions to Quartz cron expressions. This is a known format we expect.
     * 9.1.3 Result:
     * java.lang.IllegalArgumentException: Failed to parse '12 1 * ? *'. Invalid expression: ?
     */
    @Test
    public void testCronExpressionForConversionToQuartz() {
        final CronParser unixParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final CronMapper unixToQuartz = CronMapper.fromUnixToQuartz();
        String unixExpression = "12 1 * ? *";
        // Works on 5.0.5, throws IllegalArgumentException in 9.1.3
        Cron unixCron = unixParser.parse(unixExpression);
        // Goal: Convert unix expressions to quartz.
        String quartzExpression = unixToQuartz.map(unixCron).asString();
        // This is what we expected.
        assertEquals("0 12 1 * ? ? *", quartzExpression);
    }
}
