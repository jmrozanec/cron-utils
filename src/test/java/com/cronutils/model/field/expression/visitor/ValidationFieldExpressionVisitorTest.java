package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.value.SpecialChar;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationFieldExpressionVisitorTest {
    //TODO enable
    /*
    @Test(expected = IllegalArgumentException.class)
    public void testStringToIntFailsNoStringMappingAndStringNotInt() throws Exception {
        String monString = "MON";

        assertNull(stringMapping.get(monString));
        fieldConstraints.stringToInt(monString);
    }

    @Test
    public void testStringToInt() throws Exception {
        String monString = "MON";
        int monNumber = 1;

        stringMapping.put(monString, monNumber);

        assertEquals(monNumber, fieldConstraints.stringToInt(monString));
    }

    @Test
    public void testIntToIntNoMapping() throws Exception {
        int source = 1;
        assertNull(intMapping.get(source));
        assertEquals(source, fieldConstraints.intToInt(source));
    }

    @Test
    public void testIntToIntWithMapping() throws Exception {
        int source = 1;
        int dest = 0;
        intMapping.put(source, dest);

        assertEquals(dest, fieldConstraints.intToInt(source));
    }

    @Test
    public void testValidateInRangeOK() throws Exception {
        assertEquals(startRange, fieldConstraints.validateInRange(startRange));
        assertEquals(endRange, fieldConstraints.validateInRange(endRange));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInRangeOutThrowsExceptionUpperBound() throws Exception {
        fieldConstraints.validateInRange(endRange + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInRangeOutThrowsExceptionLowerBound() throws Exception {
        fieldConstraints.validateInRange(startRange - 1);
    }

    @Test
    public void testValidateSpecialCharAllowedContainsChar() throws Exception {
        SpecialChar specialChar = SpecialChar.HASH;
        specialCharSet.add(specialChar);
        fieldConstraints.validateSpecialCharAllowed(specialChar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateSpecialCharAllowedDoesNotContainChar() throws Exception {
        SpecialChar specialChar = SpecialChar.HASH;
        assertFalse(specialCharSet.contains(specialChar));
        fieldConstraints.validateSpecialCharAllowed(specialChar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEveryXBiggerThanRange() throws Exception {
        assertEquals(1, (int)new Between(new IntegerFieldValue(from), new IntegerFieldValue(to), new IntegerFieldValue(2 * to)).getEvery().getPeriod().getValue());
    }
    */
}