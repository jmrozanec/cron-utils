package com.cron.utils.parser.field;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlwaysTest {

    @Test
    public void testGetEveryDefault() throws Exception {
        assertEquals(1, new Always(FieldConstraints.nullConstraints()).getEvery().getTime());
    }

    @Test
    public void testGetEveryX() throws Exception {
        int value = 11;
        assertEquals(value, new Always(FieldConstraints.nullConstraints(), "" + value).getEvery().getTime());
    }

    @Test
    public void testNullConstraints() throws Exception {
        new Always(null);
    }
}