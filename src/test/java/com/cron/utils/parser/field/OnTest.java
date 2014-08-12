package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OnTest {
    private int time;
    private int nth;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() {
        time = 5;
        nth = 3;
        nullFieldConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
    }

    @Test
    public void testGetTime() throws Exception {
        assertEquals(time, new On(nullFieldConstraints, "" + time).getTime());
    }

    @Test
    public void testGetNth() throws Exception {
        assertEquals(nth, new On(nullFieldConstraints, String.format("%s#%s", time, nth)).getNth());
    }

    @Test(expected = RuntimeException.class)
    public void testOnlyNthFails() throws Exception {
        new On(nullFieldConstraints, String.format("#%s", nth));
    }
}