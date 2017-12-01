package com.cronutils.model.field.expression.visitor;

import java.util.Collections;

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
import com.cronutils.utils.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationFieldExpressionVisitorTest {

    private static final int DEFAULT_INT = -1;

    private static final int LOWOOR = -21;
    private static final int HIGHOOR = 999;

    private static final int LOW = 1;
    private static final int MIDDLE = 25;
    private static final int HIGH = 50;

    private FieldConstraints fieldConstraints;

    @Mock
    private StringValidations stringValidations;

    @Mock
    private StringValidations invalidStringValidations;

    private ValidationFieldExpressionVisitor strictVisitor;
    private ValidationFieldExpressionVisitor visitor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final int startRange = 0;
        final int endRange = 59;
        fieldConstraints = new FieldConstraints(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), startRange, endRange);

        when(stringValidations.removeValidChars(any(String.class))).thenReturn(StringUtils.EMPTY);
        when(invalidStringValidations.removeValidChars(any(String.class))).thenReturn("$$$");

        strictVisitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations, true);
        visitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitWithInvalidChars() {
        final ValidationFieldExpressionVisitor tempVisitor = new ValidationFieldExpressionVisitor(fieldConstraints, invalidStringValidations, true);
        final FieldExpression exp = FieldExpression.always();
        tempVisitor.visit(exp);
    }

    @Test
    public void testVisit() {
        ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        FieldExpression exp = FieldExpression.always();
        final Always always = (Always) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(always);
        verify(strictSpy, times(1)).visit(always);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between between = (Between) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Every(new IntegerFieldValue(LOW));
        final Every every = (Every) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(every);
        verify(strictSpy, times(1)).visit(every);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new And().and(between);
        final And and = (And) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(and);
        verify(strictSpy, times(1)).visit(and);
        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new On(new IntegerFieldValue(MIDDLE));
        final On on = (On) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(on);
        verify(strictSpy, times(1)).visit(on);

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = FieldExpression.questionMark();
        final QuestionMark qm = (QuestionMark) exp;
        spy.visit(exp);
        strictSpy.visit(exp);

        verify(spy, times(1)).visit(qm);
        verify(strictSpy, times(1)).visit(qm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBadExp() {
        final FieldExpression exp = new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW));
        strictVisitor.visit(exp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBadExp() {
        final FieldExpression exp = new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH));
        visitor.visit(exp);
    }

    @Test
    public void testVisitAlwaysField() {
        final FieldExpression always = FieldExpression.always();
        assertEquals(always, strictVisitor.visit(always));
        assertEquals(always, visitor.visit(always));
    }

    @Test
    public void testVisitQuestionMarkField() {
        final FieldExpression qm = FieldExpression.questionMark();
        assertEquals(qm, strictVisitor.visit(qm));
        assertEquals(qm, visitor.visit(qm));
    }

    @Test
    public void testVisitBetween() {
        Between between = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));

        between = new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));

        between = new Between(new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(MIDDLE));
        assertEquals(between, strictVisitor.visit(between));
        assertEquals(between, visitor.visit(between));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenWrongSpecialChars() {
        strictVisitor.visit(new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeBottom() {
        strictVisitor.visit(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeTop() {
        strictVisitor.visit(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORange() {
        strictVisitor.visit(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOOOrder() {
        strictVisitor.visit(new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenWrongSpecialChars() {
        visitor.visit(new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeBottom() {
        visitor.visit(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeTop() {
        visitor.visit(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORange() {
        visitor.visit(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGHOOR)));
    }

    @Test
    public void testVisitBetweenOOOrder() {
        final Between between = new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW));
        assertEquals(between, visitor.visit(between));
    }

    @Test
    public void testVisitEvery() {
        Every every = new Every(new IntegerFieldValue(MIDDLE));
        final ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        final ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        final Between between = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        every = new Every(between, new IntegerFieldValue(HIGH));
        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        verify(spy, times(1)).visit(between);
        verify(strictSpy, times(1)).visit(between);

        final On on = new On(new IntegerFieldValue(LOW));

        every = new Every(on, new IntegerFieldValue(HIGH));
        assertEquals(every, spy.visit(every));
        assertEquals(every, strictSpy.visit(every));

        verify(spy, times(1)).visit(on);
        verify(strictSpy, times(1)).visit(on);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORange() {
        strictVisitor.visit(new Every(new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(LOW)), new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGH)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(HIGHOOR)), new IntegerFieldValue(HIGH)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORange() {
        visitor.visit(new Every(new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBetween() {
        visitor.visit(new Every(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeOn() {
        visitor.visit(new Every(new On(new IntegerFieldValue(LOW)), new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadBetween() {
        strictVisitor.visit(new Every(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGH)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadOn() {
        strictVisitor.visit(new Every(new On(new IntegerFieldValue(HIGHOOR)), new IntegerFieldValue(HIGH)));
    }

    @Test
    public void testVisitOn() {
        On on = new On(new IntegerFieldValue(LOW));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(DEFAULT_INT));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(HIGH));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));

        on = new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(DEFAULT_INT));
        assertEquals(on, strictVisitor.visit(on));
        assertEquals(on, visitor.visit(on));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadTime() {
        strictVisitor.visit(new On(new IntegerFieldValue(LOWOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadNth() {
        strictVisitor.visit(new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(HIGHOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadTime() {
        visitor.visit(new On(new IntegerFieldValue(LOWOOR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadNth() {
        visitor.visit(new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(HIGHOOR)));
    }

    @Test
    public void testVisitAnd() {
        final ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        final ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);
        And and = new And();
        final Between b1 = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between b2 = new Between(new IntegerFieldValue(MIDDLE), new IntegerFieldValue(HIGH));
        final On on = new On(new IntegerFieldValue(LOW));
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
        final And and = new And();
        final Between b1 = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between b2 = new Between(new IntegerFieldValue(MIDDLE), new IntegerFieldValue(HIGHOOR));
        final On on = new On(new IntegerFieldValue(LOW));
        and.and(b1).and(b2).and(b2).and(on);
        strictVisitor.visit(and);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitAndBadExpression() {
        final And and = new And();
        final Between b1 = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between b2 = new Between(new IntegerFieldValue(MIDDLE), new IntegerFieldValue(HIGHOOR));
        final On on = new On(new IntegerFieldValue(LOW));
        and.and(b1).and(b2).and(b2).and(on);
        visitor.visit(and);
    }

    @Test
    public void testIsDefault() {
        final SpecialCharFieldValue nonIntegerFieldValue = new SpecialCharFieldValue(SpecialChar.LW);
        assertFalse(strictVisitor.isDefault(nonIntegerFieldValue));
        assertFalse(visitor.isDefault(nonIntegerFieldValue));

        IntegerFieldValue integerValue = new IntegerFieldValue(DEFAULT_INT);
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

        for (final SpecialChar sp : SpecialChar.values()) {
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
        final IntegerFieldValue integerValue = new IntegerFieldValue(81);
        assertFalse(strictVisitor.isSpecialCharNotL(integerValue));
        assertFalse(visitor.isSpecialCharNotL(integerValue));
    }

    @Test
    public void testIsInRange() {
        final SpecialCharFieldValue nonIntegerFieldValue = new SpecialCharFieldValue(SpecialChar.LW);
        strictVisitor.isInRange(nonIntegerFieldValue);
        visitor.isInRange(nonIntegerFieldValue);

        final IntegerFieldValue integerValue = new IntegerFieldValue(5);
        strictVisitor.isInRange(integerValue);
        visitor.isInRange(integerValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeOORangeStrict() {
        final IntegerFieldValue integerValue = new IntegerFieldValue(HIGHOOR);
        strictVisitor.isInRange(integerValue);
    }

}
