package com.cron.utils.model.field;

import com.cron.utils.model.field.Between;
import com.cron.utils.model.field.constraint.FieldConstraints;
import com.cron.utils.model.field.constraint.FieldConstraintsBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BetweenTest {
    private int from;
    private int to;
    private int every;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() {
        from = 1;
        to = 5;
        every = 2;
        nullFieldConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
    }

    @Test
    public void testGetFrom() throws Exception {
        assertEquals(from, new Between(nullFieldConstraints, "" + from, "" + to).getFrom());
    }

    @Test
    public void testGetTo() throws Exception {
        assertEquals(to, new Between(nullFieldConstraints, "" + from, "" + to).getTo());
    }

    @Test(expected = RuntimeException.class)
    public void testFromGreaterThanTo() throws Exception {
        new Between(nullFieldConstraints, "" + to, "" + from);
    }

    @Test(expected = RuntimeException.class)
    public void testFromEqualThanTo() throws Exception {
        new Between(nullFieldConstraints, "" + from, "" + from);
    }

    @Test
    public void testGetEveryDefault() throws Exception {
        assertEquals(1, new Between(nullFieldConstraints, "" + from, "" + to).getEvery().getTime());
    }

    @Test
    public void testGetEveryX() throws Exception {
        assertEquals(every, new Between(nullFieldConstraints, "" + from, "" + to, "" + every).getEvery().getTime());
    }

    @Test(expected = RuntimeException.class)
    public void testGetEveryXBiggerThanRange() throws Exception {
        assertEquals(1, new Between(nullFieldConstraints, "" + from, "" + to, "" + 2 * to).getEvery().getTime());
    }
}