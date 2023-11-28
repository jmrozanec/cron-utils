package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.jupiter.api.Test;

import static com.cronutils.model.CronType.UNIX;
import static com.cronutils.model.field.expression.Always.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue459Test {
    @Test
    public void testNegativeValuesNotAllowed() {
        assertThrows(RuntimeException.class, () -> CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(UNIX))
                .withDoM(always())
                .withMonth(always())
                .withDoW(always())
                .withHour(on(-1))
                .withMinute(on(5))
                .instance());
    }
}
