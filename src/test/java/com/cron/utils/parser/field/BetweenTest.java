package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BetweenTest {
    private int from;
    private int to;
    private int every;

    @Before
    public void setUp() {
        from = 1;
        to = 5;
        every = 2;
    }

    @Test
    public void testGetFrom() throws Exception {
        assertEquals(from, new Between(null, "" + from, "" + to).getFrom());
    }

    @Test
    public void testGetTo() throws Exception {
        assertEquals(to, new Between(null, "" + from, "" + to).getTo());
    }

    @Test(expected = RuntimeException.class)
    public void testFromGreaterThanTo() throws Exception {
        new Between(null, "" + to, "" + from);
    }

    @Test(expected = RuntimeException.class)
    public void testFromEqualThanTo() throws Exception {
        new Between(null, "" + from, "" + from);
    }

    @Test
    public void testGetEveryDefault() throws Exception {
        assertEquals(1, new Between(null, "" + from, "" + to).getEvery().getTime());
    }

    @Test
    public void testGetEveryX() throws Exception {
        assertEquals(every, new Between(null, "" + from, "" + to, "" + every).getEvery().getTime());
    }

    @Test(expected = RuntimeException.class)
    public void testGetEveryXBiggerThanRange() throws Exception {
        assertEquals(1, new Between(null, "" + from, "" + to, "" + 2 * to).getEvery().getTime());
    }
}