package com.cronutils.model.field.constraints;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.SpecialChar;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class FieldConstraintsTest {

    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private Set<SpecialChar> specialCharSet;
    private int startRange;
    private int endRange;

    private FieldConstraints fieldConstraints;

    @Before
    public void setUp() throws Exception {
        intMapping = Maps.newHashMap();
        stringMapping = Maps.newHashMap();
        specialCharSet = Sets.newHashSet();
        startRange = 0;
        endRange = 59;
        fieldConstraints = new FieldConstraints(stringMapping, intMapping, specialCharSet, startRange, endRange);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorStringMappingNull() throws Exception {
        new FieldConstraints(null, intMapping, specialCharSet, startRange, endRange);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorIntMappingNull() throws Exception {
        new FieldConstraints(stringMapping, null, specialCharSet, startRange, endRange);
    }

    @Test(expected = NullPointerException.class)
    public void testSpecialCharsSetNull() throws Exception {
        new FieldConstraints(stringMapping, intMapping, null, startRange, endRange);
    }

    @Test(expected = NumberFormatException.class)
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

    @Test(expected = RuntimeException.class)
    public void testValidateInRangeOutThrowsExceptionUpperBound() throws Exception {
        fieldConstraints.validateInRange(endRange + 1);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateInRangeOutThrowsExceptionLowerBound() throws Exception {
        fieldConstraints.validateInRange(startRange - 1);
    }

    @Test
    public void testValidateSpecialCharAllowedContainsChar() throws Exception {
        SpecialChar specialChar = SpecialChar.HASH;
        specialCharSet.add(specialChar);
        fieldConstraints.validateSpecialCharAllowed(specialChar);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateSpecialCharAllowedDoesNotContainChar() throws Exception {
        SpecialChar specialChar = SpecialChar.HASH;
        assertFalse(specialCharSet.contains(specialChar));
        fieldConstraints.validateSpecialCharAllowed(specialChar);
    }
}