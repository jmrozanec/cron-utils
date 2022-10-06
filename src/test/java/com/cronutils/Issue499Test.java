package com.cronutils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import org.junit.jupiter.api.Test;

class Issue499Test {
    /**
     * We want to convert Unix cron expressions to Quartz cron expressions. We
     * expect an exception: java.lang.IllegalArgumentException:
     * Failed to parse '12 1 * ? *'. Invalid expression: ?
     * Given question marks are not supported at Unix crons. See:
     * https://github.com/jmrozanec/cron-utils/issues/499
     */
    @Test
    void testCronExpressionForConversionToQuartz() {
        final CronParser unixParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final String unixExpression = "12 1 * ? *";
        // Works on 5.0.5, throws IllegalArgumentException in 9.1.3
        assertThrows(IllegalArgumentException.class, () -> unixParser.parse(unixExpression));
    }
}
