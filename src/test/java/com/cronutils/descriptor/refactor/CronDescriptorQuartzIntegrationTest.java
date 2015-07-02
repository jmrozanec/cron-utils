package com.cronutils.descriptor.refactor;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CronDescriptorQuartzIntegrationTest {
    private CronParser parser;
    CronDescriptor descriptor;

    @Before
    public void setUp() throws Exception {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        descriptor = CronDescriptor.instance();
    }

    @Test
    public void testDescribeEverySecond() throws Exception {
        assertEquals("every second", descriptor.describe(parser.parse("* * * * * *")));
    }

    @Test
    public void testDescribeEveryMinute() throws Exception {
        assertExpression("0 * * * * *", "every minute");
    }

    @Test
    public void testDescribeEveryHour() throws Exception {
        assertExpression("0 0 * * * *", "every hour");
    }

    @Test
    public void testDescribeEveryDayOfWeek() throws Exception {
        assertExpression("0 0 0 * * *", "every day of week");
    }

    //TODO fix: we get: every day of week on day 1 every month
    public void testDescribeEveryDayOfMonth() throws Exception {
        assertExpression("0 0 0 1 * *", "day 1 of every month");//TODO ideally: first day of every month
    }

    public void testDescribeEveryYear() throws Exception {
        assertExpression("0 0 0 1 1 *", "first day in the year");
    }

    @Test
    public void testEvery45Seconds(){
        assertExpression("*/45 * * * * *", "every 45 seconds");
    }

    @Test
    public void testEveryHour(){
        assertExpression("0 0 * * * ?", "every hour");
        assertExpression("0 0 0/1 * * ?", "every hour");
    }

    //TODO fix: now getting: between 5 minute at 14 hs every day of week
    public void testEveryFiveMinutesBetween14and15EveryDay() throws Exception {
        //check http://english.stackexchange.com/questions/56869/24-hour-time-how-to-say-it
        assertExpression("0 0/5 14 * * ?", "every 5 minutes at 14");
    }

    private void assertExpression(String cron, String description){
        assertEquals(description, descriptor.describe(parser.parse(cron)));
    }
}