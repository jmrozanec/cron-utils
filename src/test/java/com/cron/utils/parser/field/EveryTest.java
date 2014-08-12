package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EveryTest {
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() {
        nullFieldConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
    }

    @Test
    public void testGetTime() throws Exception {
        int every = 5;
        assertEquals(every, new Every(nullFieldConstraints, "" + every).getTime());
    }

    @Test
    public void testGetTimeNull() throws Exception {
        assertEquals(1, new Every(nullFieldConstraints, null).getTime());
    }
}