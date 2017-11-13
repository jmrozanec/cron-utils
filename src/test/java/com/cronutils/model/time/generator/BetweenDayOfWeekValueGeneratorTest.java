package com.cronutils.model.time.generator;

import java.util.List;

import org.junit.Test;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertTrue;

public class BetweenDayOfWeekValueGeneratorTest {

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        //cron expression: DoW 1-5,
        //on February 2016: 1-5, 8-12, 15-19, 22-26, 29
        CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK, new Between(new IntegerFieldValue(1), new IntegerFieldValue(5)),
                FieldConstraintsBuilder.instance().createConstraintsInstance());
        BetweenDayOfWeekValueGenerator generator = new BetweenDayOfWeekValueGenerator(cronField, 2016, 2, new WeekDay(1, true));
        List<Integer> values = generator.generateCandidates(1, 29);
        validateInterval(1, 5, values);
        validateInterval(8, 12, values);
        validateInterval(15, 19, values);
        validateInterval(22, 26, values);
        assertTrue(values.contains(29));
    }

    private void validateInterval(int start, int end, List<Integer> values) {
        for (int j = start; j < end + 1; j++) {
            assertTrue(String.format("%s not contained in values", j), values.contains(j));
        }
    }
}
