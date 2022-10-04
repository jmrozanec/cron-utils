package com.cronutils.model.field.expression.visitor;

import com.cronutils.StringValidations;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.cronutils.utils.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        fieldConstraints = new FieldConstraints(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), startRange, endRange, true);

        when(stringValidations.removeValidChars(any(String.class))).thenReturn(StringUtils.EMPTY);
        when(invalidStringValidations.removeValidChars(any(String.class))).thenReturn("$$$");

        strictVisitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations);
        visitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitWithInvalidChars() {
        final ValidationFieldExpressionVisitor tempVisitor = new ValidationFieldExpressionVisitor(fieldConstraints, invalidStringValidations);
        final FieldExpression exp = FieldExpression.always();
        exp.accept(tempVisitor);
    }

    @Test
    public void testVisit() {
        ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        FieldExpression exp = FieldExpression.always();
        final Always always = (Always) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        always.accept(verify(spy, times(1)));
        always.accept(verify(strictSpy, times(1)));

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between between = (Between) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        between.accept(verify(spy, times(1)));
        between.accept(verify(strictSpy, times(1)));

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new Every(new IntegerFieldValue(LOW));
        final Every every = (Every) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        every.accept(verify(spy, times(1)));
        every.accept(verify(strictSpy, times(1)));

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new And().and(between);
        final And and = (And) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        and.accept(verify(spy, times(1)));
        and.accept(verify(strictSpy, times(1)));
        between.accept(verify(spy, times(1)));
        between.accept(verify(strictSpy, times(1)));

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = new On(new IntegerFieldValue(MIDDLE));
        final On on = (On) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        on.accept(verify(spy, times(1)));
        on.accept(verify(strictSpy, times(1)));

        spy = Mockito.spy(visitor);
        strictSpy = Mockito.spy(strictVisitor);

        exp = FieldExpression.questionMark();
        final QuestionMark qm = (QuestionMark) exp;
        exp.accept(spy);
        exp.accept(strictSpy);

        qm.accept(verify(spy, times(1)));
        qm.accept(verify(strictSpy, times(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBadExp() {
        final FieldExpression exp = new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW));
        exp.accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBadExp() {
        final FieldExpression exp = new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH));
        exp.accept(visitor);
    }

    @Test
    public void testVisitAlwaysField() {
        final FieldExpression always = FieldExpression.always();
        assertEquals(always, always.accept(strictVisitor));
        assertEquals(always, always.accept(visitor));
    }

    @Test
    public void testVisitQuestionMarkField() {
        final FieldExpression qm = FieldExpression.questionMark();
        assertEquals(qm, qm.accept(strictVisitor));
        assertEquals(qm, qm.accept(visitor));
    }

    @Test
    public void testVisitBetween() {
        Between between = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        assertEquals(between, between.accept(strictVisitor));
        assertEquals(between, between.accept(visitor));

        between = new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(between, between.accept(strictVisitor));
        assertEquals(between, between.accept(visitor));

        between = new Between(new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(MIDDLE));
        assertEquals(between, between.accept(strictVisitor));
        assertEquals(between, between.accept(visitor));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenWrongSpecialChars() {
        new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW)).accept(strictVisitor);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeBottom() {
        new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORangeTop() {
        new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOORange() {
        new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitBetweenOOOrder() {
        new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenWrongSpecialChars() {
        new Between(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW)).accept(visitor);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeBottom() {
        new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGH)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORangeTop() {
        new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(HIGHOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOORange() {
        new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(HIGHOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitBetweenOOOrderStrict() {
        final int startRange = 0;
        final int endRange = 59;
        fieldConstraints = new FieldConstraints(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), startRange, endRange, true);
        visitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations);

        final Between between = new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW));
        assertEquals(between, between.accept(visitor));
    }

    @Test
    public void testVisitBetweenOOOrderNonStrict() {
        final int startRange = 0;
        final int endRange = 59;
        fieldConstraints = new FieldConstraints(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), startRange, endRange, false);
        visitor = new ValidationFieldExpressionVisitor(fieldConstraints, stringValidations);

        final Between between = new Between(new IntegerFieldValue(HIGH), new IntegerFieldValue(LOW));
        assertEquals(between, between.accept(visitor));
    }

    @Test
    public void testVisitEvery() {
        Every every = new Every(new IntegerFieldValue(MIDDLE));
        final ValidationFieldExpressionVisitor spy = Mockito.spy(visitor);
        final ValidationFieldExpressionVisitor strictSpy = Mockito.spy(strictVisitor);

        assertEquals(every, every.accept(spy));
        assertEquals(every, every.accept(strictSpy));

        final Between between = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        every = new Every(between, new IntegerFieldValue(HIGH));
        assertEquals(every, every.accept(spy));
        assertEquals(every, every.accept(strictSpy));

        between.accept(verify(spy, times(1)));
        between.accept(verify(strictSpy, times(1)));

        final On on = new On(new IntegerFieldValue(LOW));

        every = new Every(on, new IntegerFieldValue(HIGH));
        assertEquals(every, every.accept(spy));
        assertEquals(every, every.accept(strictSpy));

        on.accept(verify(spy, times(1)));
        on.accept(verify(strictSpy, times(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORange() {
        new Every(new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBetween() {
        new Every(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeOn() {
        new Every(new On(new IntegerFieldValue(LOW)), new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadBetween() {
        new Every(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGH)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitEveryOORangeBadOn() {
        new Every(new On(new IntegerFieldValue(HIGHOOR)), new IntegerFieldValue(HIGH)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORange() {
        new Every(new IntegerFieldValue(HIGHOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBetween() {
        new Every(new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGHOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeOn() {
        new Every(new On(new IntegerFieldValue(LOW)), new IntegerFieldValue(HIGHOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadBetween() {
        new Every(new Between(new IntegerFieldValue(LOWOOR), new IntegerFieldValue(MIDDLE)),
                new IntegerFieldValue(HIGH)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitEveryOORangeBadOn() {
        new Every(new On(new IntegerFieldValue(HIGHOOR)), new IntegerFieldValue(HIGH)).accept(strictVisitor);
    }

    @Test
    public void testVisitOn() {
        On on = new On(new IntegerFieldValue(LOW));
        assertEquals(on, on.accept(strictVisitor));
        assertEquals(on, on.accept(visitor));

        on = new On(new IntegerFieldValue(DEFAULT_INT));
        assertEquals(on, on.accept(strictVisitor));
        assertEquals(on, on.accept(visitor));

        on = new On(new SpecialCharFieldValue(SpecialChar.L));
        assertEquals(on, on.accept(strictVisitor));
        assertEquals(on, on.accept(visitor));

        on = new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(HIGH));
        assertEquals(on, on.accept(strictVisitor));
        assertEquals(on, on.accept(visitor));

        on = new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(DEFAULT_INT));
        assertEquals(on, on.accept(strictVisitor));
        assertEquals(on, on.accept(visitor));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadTime() {
        new On(new IntegerFieldValue(LOWOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitOnBadNth() {
        new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(HIGHOOR)).accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadTime() {
        new On(new IntegerFieldValue(LOWOOR)).accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitOnBadNth() {
        new On(new IntegerFieldValue(LOW), new SpecialCharFieldValue(SpecialChar.LW),
                new IntegerFieldValue(HIGHOOR)).accept(visitor);
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
        assertEquals(and, and.accept(spy));
        assertEquals(and, and.accept(strictSpy));

        b1.accept(verify(spy, times(1)));
        b2.accept(verify(spy, times(2)));
        on.accept(verify(spy, times(1)));
        b1.accept(verify(strictSpy, times(1)));
        b2.accept(verify(strictSpy, times(2)));
        on.accept(verify(strictSpy, times(1)));
    }

    @Test
    public void testVisitEmptyAnd(){
        And and = new And();
        assertEquals(and, and.accept(visitor));
        assertEquals(and, and.accept(strictVisitor));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictVisitAndBadExpression() {
        final And and = new And();
        final Between b1 = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between b2 = new Between(new IntegerFieldValue(MIDDLE), new IntegerFieldValue(HIGHOOR));
        final On on = new On(new IntegerFieldValue(LOW));
        and.and(b1).and(b2).and(b2).and(on);
        and.accept(strictVisitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitAndBadExpression() {
        final And and = new And();
        final Between b1 = new Between(new IntegerFieldValue(LOW), new IntegerFieldValue(MIDDLE));
        final Between b2 = new Between(new IntegerFieldValue(MIDDLE), new IntegerFieldValue(HIGHOOR));
        final On on = new On(new IntegerFieldValue(LOW));
        and.and(b1).and(b2).and(b2).and(on);
        and.accept(visitor);
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
