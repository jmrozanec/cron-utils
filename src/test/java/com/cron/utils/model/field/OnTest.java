package com.cron.utils.model.field;

import com.cron.utils.model.field.constraint.FieldConstraints;
import com.cron.utils.model.field.constraint.FieldConstraintsBuilder;
import com.cron.utils.model.field.On;
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
        nullFieldConstraints =
                FieldConstraintsBuilder.instance()
                        .addHashSupport()
                        .addLSupport()
                        .addWSupport()
                        .createConstraintsInstance();
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

    @Test
    public void testAsStringJustNumber(){
        String expression = "3";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringSpecialCharW(){
        String expression = "1W";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringSpecialCharL(){
        String expression = "L";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringWithNth(){
        String expression = "3#4";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }
}