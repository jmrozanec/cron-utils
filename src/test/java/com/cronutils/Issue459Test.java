package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.Test;

import static com.cronutils.model.CronType.UNIX;
import static com.cronutils.model.field.expression.FieldExpression.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;

public class Issue459Test {
    @Test(expected = RuntimeException.class)
    public void testNegativeValuesNotAllowed() {
        CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(UNIX))
                .withDoM(always())
                .withMonth(always())
                .withDoW(always())
                .withHour(on(-1))
                .withMinute(on(5))
                .instance();
    }
}
