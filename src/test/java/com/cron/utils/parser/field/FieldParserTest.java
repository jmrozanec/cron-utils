package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldParserTest {
    private FieldParser parser;

    @Before
    public void setUp() {
        parser = new FieldParser(FieldConstraintsBuilder.instance().createConstraintsInstance());
    }

    @Test
    public void testParseAlways() throws Exception {
        assertEquals(1, ((Always) parser.parse("*")).getEvery().getTime());
    }

    @Test
    public void testParseAlwaysEveryX() throws Exception {
        int every = 5;
        assertEquals(every, ((Every) parser.parse("*/" + 5)).getTime());
    }

    @Test
    public void testParseOn() throws Exception {
        int on = 5;
        assertEquals(5, ((On) parser.parse("" + 5)).getTime());
    }

    @Test
    public void testParseAnd() throws Exception {
        int on1 = 3;
        int on2 = 4;
        And and = (And) parser.parse(String.format("%s,%s", on1, on2));
        assertEquals(2, and.getExpressions().size());
        assertEquals(on1, ((On) and.getExpressions().get(0)).getTime());
        assertEquals(on2, ((On) and.getExpressions().get(1)).getTime());
    }

    @Test
    public void testParseBetween() throws Exception {
        int from = 3;
        int to = 4;
        Between between = (Between) parser.parse(String.format("%s-%s", from, to));
        assertEquals(from, between.getFrom());
        assertEquals(to, between.getTo());
    }

    @Test
    public void testParseBetweenEveryX() throws Exception {
        int from = 10;
        int to = 40;
        int every = 5;
        Between between = (Between) parser.parse(String.format("%s-%s/%s", from, to, every));
        assertEquals(from, between.getFrom());
        assertEquals(to, between.getTo());
        assertEquals(every, between.getEvery().getTime());
    }

    @Test(expected = NullPointerException.class)
    public void testCostructorNullConstraints() throws Exception {
        new FieldParser(null);
    }
}