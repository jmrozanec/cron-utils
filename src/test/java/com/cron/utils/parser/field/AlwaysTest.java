package com.cron.utils.parser.field;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlwaysTest {

    @Test
    public void testGetEveryDefault() throws Exception {
        assertEquals(1,
                new Always(
                        FieldConstraintsBuilder.instance().createConstraintsInstance()
                ).getEvery().getTime());
    }

    @Test
    public void testGetEveryX() throws Exception {
        int value = 11;
        assertEquals(value,
                new Always(
                        FieldConstraintsBuilder.instance().createConstraintsInstance(),
                        "" + value
                ).getEvery().getTime());
    }

    @Test(expected = NullPointerException.class)
    public void testNullConstraints() {
        new Always(null);
    }
}