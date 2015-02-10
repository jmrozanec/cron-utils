package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.FieldExpression;
import com.cronutils.model.field.On;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OnDayOfMonthValueGeneratorTest {
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private int year = 2015;
    private int month = 2;
    private int day = 3;

    @Before
    public void setUp(){
        FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
        fieldValueGenerator = new OnDayOfMonthValueGenerator(new CronField(CronFieldName.DAY_OF_MONTH, new On(constraints, ""+3)), year, month);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws Exception {
        assertEquals(day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        assertEquals(day, fieldValueGenerator.generatePreviousValue(day+1));
        fieldValueGenerator.generatePreviousValue(day);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1,32);
        assertEquals(1, candidates.size());
        assertEquals(day, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(day));
        assertFalse(fieldValueGenerator.isMatch(day-1));
    }

    @Test
    public void testMatchesFieldExpressionClass() throws Exception {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(On.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesOn() throws Exception {
        new OnDayOfMonthValueGenerator(new CronField(CronFieldName.YEAR, mock(FieldExpression.class)), year, month);
    }
}
