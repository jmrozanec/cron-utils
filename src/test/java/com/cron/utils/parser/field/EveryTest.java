package com.cron.utils.parser.field;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EveryTest {

    @Test
    public void testGetTime() throws Exception {
        int every = 5;
        assertEquals(every, new Every(null, "" + every).getTime());
    }

    @Test
    public void testGetTimeNull() throws Exception {
        assertEquals(1, new Every(null, null).getTime());
    }
}