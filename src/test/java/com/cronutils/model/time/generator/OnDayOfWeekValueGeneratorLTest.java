package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.On;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OnDayOfWeekValueGeneratorLTest {
    FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
    private OnDayOfWeekValueGenerator fieldValueGenerator;
    private int year = 2015;
    private int month = 2;
    private int value = 6;
    private int day = 27;

    @Before
    public void setUp(){
        //last friday of month (6L) is 27
        fieldValueGenerator = new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK, new On(constraints, String.format("%sL", value))), year, month);
    }

    @Test
    public void testGenerateNextValue() throws Exception {
        //TODO fix this test
        //assertEquals(day, fieldValueGenerator.generateNextValue(1));
        //fieldValueGenerator.generateNextValue(day);
    }

    @Test
    public void testGeneratePreviousValue() throws Exception {

    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {

    }

    @Test
    public void testIsMatch() throws Exception {

    }


    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstance(int month, int day) {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK, new On(constraints, String.format("%sW", day))), year, month);
    }
}
