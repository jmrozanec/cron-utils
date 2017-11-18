package com.cronutils.model.field.expression.visitor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.cronutils.StringValidations;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationFieldExpressionVisitorTest {

    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private Set<SpecialChar> specialCharSet;
    private int startRange;
    private int endRange;

    private int defaultInt = -1;

    private int lowOOR = -21;
    private int highOOR = 999;

    private int low = 1;
    private int middle = 25;
    private int high = 50;

    private FieldConstraints fieldConstraints;

    @Mock
    private StringValidations stringValidations;

    @Mock
    private StringValidations invalidStringValidations;

    private ValidationFieldExpressionVisitor strictVisitor;
    private ValidationFieldExpressionVisitor visitor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        intMapping = Collections.emptyMap();
        stringMapping = Collections.emptyMap();
        specialCharSet = Collections.emptySet();
        startRange = 0;
        endRange = 59;
        fieldConstraints = new FieldConstraints(stringMapping, intMapping, specialCharSet, startRange, endRange);

        when(stringValidations.removeValidChars(any(String.class))).thenReturn("");
        when(invalidStringValidations.removeValidChars(any(String.class))).thenReturn("$$$");

        strictVisitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations, true);
        visitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitWithInvalidChars() {
        ValidationFieldExpressionVisitor visitor = new ValidationFieldExpressionVisitor(fieldConstraints, invalidStringValidations, true);
        FieldExpression exp = new Always();
        visitor.visit(exp);
    }

    @Test
    public void testVisit() {
        ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        FieldExpression exp = new Always();
        Always always = (Always) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(always);
        verify(strictSpy, times(1)).visit(always);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        Between between = (Between) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Every(new IntegerFieldValue(low));
        Every every = (Every) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(every);
        verify(strictSpy, times(1)).visit(every);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new And().and(between);
        And and = (And) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(and);
        verify(strictSpy, times(1)).visit(and);
        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new On(new IntegerFieldValue(middle));
        On on = (On) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(on);
        verify(strictSpy, times(1)).visit(on);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new QuestionMark();
        QuestionMark qm = (QuestionMark) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(qm);
        verify(strictSpy, times(1)).visit(qm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBadExp() {
        FieldExpression exp = new Between(new IntegerFieldValue(high), new IntegerFieldValue(low));
        strictVisitor.visit(exp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBadExp() {
        FieldExpression exp = new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(high));
        visitor.visit(exp);
    }

    @Test
    public void testVisitAlwaysField() {
        Always always = new Always();
        assertEquals(always, strictVisitor.visit(always));
        assertEquals(always, visitor.visit(always));
    }

    @Test
    public void testVisitQuestionMarkField() {
        QuestionMark qm = new QuestionMark();
        assertEquals(qm, strictVisitor.visit(qm));
        assertEquals(qm, visitor.visit(qm));
    }

    @Test
    public void testVisitBetween() {
        Between between = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));

        between = new Between(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));

        between = new Between(new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(middle));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenWrongSpecialChars() {
        strictVisitor.visit(new Between(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.LW)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeBottom() {
        strictVisitor.visit(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(high)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeTop() {
        strictVisitor.visit(new Between(new IntegerFieldValue(low), new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORange() {
        strictVisitor.visit(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOOOrder() {
        strictVisitor.visit(new Between(new IntegerFieldValue(high), new IntegerFieldValue(low)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenWrongSpecialChars() {
        visitor.visit(new Between(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.LW)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeBottom() {
        visitor.visit(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(high)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeTop() {
        visitor.visit(new Between(new IntegerFieldValue(low), new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORange() {
        visitor.visit(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(highOOR)));
    }

    @Test
    public void testVisitBetweenOOOrder() {
        Between between = new Between(new IntegerFieldValue(high), new IntegerFieldValue(low));
        assertEquals(between, visitor.visit(between));
    }

    @Test
    public void testVisitEvery() {
        Every every = new Every(new IntegerFieldValue(middle));
        ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        Between between = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        every = new Every(between, new IntegerFieldValue(high));
        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        On on = new On(new IntegerFieldValue(low));

        every = new Every(on, new IntegerFieldValue(high));
        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        verify(spy, times(1)).visit(on);
        verify(strictSpy, times(1)).visit(on);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORange() {
        strictVisitor.visit(new Every(new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle)),
                new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(low)), new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(middle)),
                new IntegerFieldValue(high)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(highOOR)), new IntegerFieldValue(high)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORange() {
        visitor.visit(new Every(new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBetween() {
        visitor.visit(new Every(new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle)),
                new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeOn() {
        visitor.visit(new Every(new On(new IntegerFieldValue(low)), new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(lowOOR), new IntegerFieldValue(middle)),
                new IntegerFieldValue(high)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(highOOR)), new IntegerFieldValue(high)));
    }

    @Test
    public void testVisitOn() {
        On on = new On(new IntegerFieldValue(low));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(defaultInt));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(high));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(defaultInt));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadTime() {
        strictVisitor.visit(new On(new IntegerFieldValue(lowOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadNth() {
        strictVisitor.visit(new On(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(highOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadTime() {
        visitor.visit(new On(new IntegerFieldValue(lowOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadNth() {
        visitor.visit(new On(new IntegerFieldValue(low), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(highOOR)));
    }

    @Test
    public void testVisitAnd() {
        ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);
        And and = new And();
        Between b1 = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        Between b2 = new Between(new IntegerFieldValue(middle), new IntegerFieldValue(high));
        On on = new On(new IntegerFieldValue(low));
        and.and(b1).and(b2).and(b2).and(on);
        assertEquals(and, spy.visit(and));
        assertEquals(and, strictSpy.visit(and));

        verify(spy, times(1)).visit(b1);
        verify(spy, times(2)).visit(b2);
        verify(spy, times(1)).visit(on);
        verify(strictSpy, times(1)).visit(b1);
        verify(strictSpy, times(2)).visit(b2);
        verify(strictSpy, times(1)).visit(on);

        and = new And();
        assertEquals(and, visitor.visit(and));
        assertEquals(and, strictVisitor.visit(and));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitAndBadExpression() {
        And and = new And();
        Between b1 = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        Between b2 = new Between(new IntegerFieldValue(middle), new IntegerFieldValue(highOOR));
        On on = new On(new IntegerFieldValue(low));
        and.and(b1).and(b2).and(b2).and(on);
        strictVisitor.visit(and);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitAndBadExpression() {
        And and = new And();
        Between b1 = new Between(new IntegerFieldValue(low), new IntegerFieldValue(middle));
        Between b2 = new Between(new IntegerFieldValue(middle), new IntegerFieldValue(highOOR));
        On on = new On(new IntegerFieldValue(low));
        and.and(b1).and(b2).and(b2).and(on);
        visitor.visit(and);
    }

    @Test
    public void testIsDefault() {
        SpecialCharFieldValue nonIntegerFieldValue = new SpecialCharFieldValue(SpecialChar.LW);
        assertFalse(strictVisitor.isDefault(nonIntegerFieldValue));
        assertFalse(visitor.isDefault(nonIntegerFieldValue));

        IntegerFieldValue integerValue = new IntegerFieldValue(defaultInt);
        assertTrue(strictVisitor.isDefault(integerValue));
        assertTrue(visitor.isDefault(integerValue));
        integerValue = new IntegerFieldValue(0);
        assertFalse(strictVisitor.isDefault(integerValue));
        assertFalse(visitor.isDefault(integerValue));
        integerValue = new IntegerFieldValue(99);
        assertFalse(strictVisitor.isDefault(integerValue));
        assertFalse(visitor.isDefault(integerValue));
        integerValue = new IntegerFieldValue(-99);
        assertFalse(strictVisitor.isDefault(integerValue));
        assertFalse(visitor.isDefault(integerValue));
    }

    @Test
    public void testIsSpecialCharNotL() {
        SpecialCharFieldValue specialCharFieldValue;

        for (SpecialChar sp : SpecialChar.values()) {
            specialCharFieldValue = new SpecialCharFieldValue(sp);
            if (sp == SpecialChar.L) {
                assertFalse(strictVisitor.isSpecialCharNotL(specialCharFieldValue));
                assertFalse(visitor.isSpecialCharNotL(specialCharFieldValue));
            } else {
                assertTrue(strictVisitor.isSpecialCharNotL(specialCharFieldValue));
                assertTrue(visitor.isSpecialCharNotL(specialCharFieldValue));
            }
        }
    }

    @Test
    public void testIsSpecialCharNotLWithIntegerFieldValue() {
        IntegerFieldValue integerValue = new IntegerFieldValue(81);
        assertFalse(strictVisitor.isSpecialCharNotL(integerValue));
        assertFalse(visitor.isSpecialCharNotL(integerValue));
    }

    @Test
    public void testIsInRange() {
        SpecialCharFieldValue nonIntegerFieldValue = new SpecialCharFieldValue(SpecialChar.LW);
        strictVisitor.isInRange(nonIntegerFieldValue);
        visitor.isInRange(nonIntegerFieldValue);

        IntegerFieldValue integerValue = new IntegerFieldValue(5);
        strictVisitor.isInRange(integerValue);
        visitor.isInRange(integerValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeOORangeStrict() {
        IntegerFieldValue integerValue = new IntegerFieldValue(highOOR);
        strictVisitor.isInRange(integerValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeOORange() {
        IntegerFieldValue integerValue = new IntegerFieldValue(highOOR);
        strictVisitor.isInRange(integerValue);
    }

}
