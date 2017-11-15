package com.cronutils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class Issue143Test {
    private CronParser parser;
    private ZonedDateTime currentDateTime;

    @Before
    public void setUp() throws Exception {
        // Make sure that current date is before Dec-31
        currentDateTime = ZonedDateTime.of(LocalDateTime.of(2016, 12, 20, 12, 00),
                ZoneId.systemDefault());

        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testCase1() {
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 12 ? *"));
        ZonedDateTime actual = et.lastExecution(currentDateTime).get();

        ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2015, 12, 31, 12, 00),
                ZoneId.systemDefault());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCase2() {
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 ? 12 SAT#5 *"));
        ZonedDateTime actual = et.lastExecution(currentDateTime).get();

        ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2012, 12, 29, 12, 00),
                ZoneId.systemDefault());
        Assert.assertEquals(expected, actual);
    }

    //    @Test
    public void testCase3() {
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 1/1 ? *"));
        ZonedDateTime actual = et.lastExecution(currentDateTime).get();

        ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2015, 12, 31, 12, 00),
                ZoneId.systemDefault());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCase4() {
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 ? 1/1 SAT#5 *"));
        ZonedDateTime actual = et.lastExecution(currentDateTime).get();

        ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2016, 10, 29, 12, 00),
                ZoneId.systemDefault());
        Assert.assertEquals(expected, actual);
    }

}
