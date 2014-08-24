package com.cron.utils.model.field;

import com.cron.utils.model.field.Every;
import com.cron.utils.model.field.constraint.FieldConstraints;
import com.cron.utils.model.field.constraint.FieldConstraintsBuilder;
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