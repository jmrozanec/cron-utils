package com.cronutils.model.time.generator;

import com.cronutils.model.field.*;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class OnDayOfMonthValueGeneratorLTest {
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private int year = 2015;
    private int month = 2;
    private int lastDayInMonth = new DateTime(2015, 2, 1, 1, 1).dayOfMonth().getMaximumValue();

    @Before
    public void setUp(){
        FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
        fieldValueGenerator = new OnDayOfMonthValueGenerator(new CronField(CronFieldName.DAY_OF_MONTH, new On(constraints, "L")), year, month);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws Exception {
        assertEquals(lastDayInMonth, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(lastDayInMonth);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        assertEquals(lastDayInMonth, fieldValueGenerator.generatePreviousValue(lastDayInMonth+1));
        fieldValueGenerator.generatePreviousValue(lastDayInMonth);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1,32);
        assertEquals(1, candidates.size());
        assertEquals(lastDayInMonth, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(lastDayInMonth));
        assertFalse(fieldValueGenerator.isMatch(lastDayInMonth-1));
    }
}