package com.cron.utils.descriptor;

import com.cron.utils.CronFieldName;
import com.cron.utils.model.Cron;
import com.cron.utils.parser.field.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorTest {

    private CronDescriptor descriptor;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        nullFieldConstraints =
                FieldConstraintsBuilder.instance()
                        .addHashSupport()
                        .addLSupport()
                        .addWSupport()
                        .createConstraintsInstance();
    }

    @Test
    public void testDescribeEveryXTimeUnits() throws Exception {
        int time = 3;
        Every expression = new Every(nullFieldConstraints, "" + time);
        assertEquals(String.format("every %s seconds", time), descriptor.describe(
                        new Cron(Lists.asList(new CronField(CronFieldName.SECOND, expression), new CronField[]{}))
                )
        );
        assertEquals(String.format("every %s minutes", time), descriptor.describe(
                        new Cron(Lists.asList(new CronField(CronFieldName.MINUTE, expression), new CronField[]{}))
                )
        );
        List<CronField> params = Lists.newArrayList();
        params.add(new CronField(CronFieldName.HOUR, expression));
        params.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + time)));
        assertEquals(String.format("every %s hours at minute %s", time, time), descriptor.describe(new Cron(params)));
    }

    @Test
    public void testDescribeEveryXMinutesBetweenTime() throws Exception {
        int hour = 11;
        int start = 0;
        int end = 10;
        Between expression = new Between(nullFieldConstraints, "" + start, "" + end);
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.MINUTE, expression));
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        assertEquals(String.format("every minute between %s:%02d and %s:%02d", hour, start, hour, end), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testDescribeAtXTimeBetweenDaysOfWeek() throws Exception {
        int hour = 11;
        int minute = 30;
        int start = 2;
        int end = 6;
        Between expression = new Between(nullFieldConstraints, "" + start, "" + end);
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, expression));
        assertEquals(String.format("at %s:%s every day between Tuesday and Saturday", hour, minute), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testDescribeAtXHours() throws Exception {
        int hour = 11;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.SECOND, new Always(nullFieldConstraints)));
        assertEquals(String.format("at %s:00", hour), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testEverySecondInMonth() throws Exception {
        int month = 2;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.SECOND, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.MONTH, new On(nullFieldConstraints, "" + month)));
        assertEquals("every second at February month", descriptor.describe(new Cron(results)));
    }

    @Test
    public void testEveryMinuteBetweenMonths() throws Exception {
        int monthStart = 2;
        int monthEnd = 3;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronField(CronFieldName.MONTH, new Between(nullFieldConstraints, "" + monthStart, "" + monthEnd)));
        assertEquals("every minute every month between February and March", descriptor.describe(new Cron(results)));
    }

    @Test
    public void testLastDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, new On(nullFieldConstraints, String.format("%sL", dayOfWeek))));
        assertEquals(String.format("at %s:%s last Tuesday of every month", hour, minute), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testNthDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, new On(nullFieldConstraints, String.format("%s#%s", dayOfWeek, dayOfWeek))));
        assertEquals(String.format("at %s:%s Tuesday %s of every month", hour, minute, dayOfWeek), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testLastDayOfMonth() throws Exception {
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronField(CronFieldName.DAY_OF_MONTH, new On(nullFieldConstraints, "L")));
        assertEquals(String.format("at %s:%s last day of the month", hour, minute), descriptor.describe(new Cron(results)));
    }

    @Test
    public void testNearestWeekdayToNthOfMonth() throws Exception {
        int dayOfMonth = 22;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronField(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronField(CronFieldName.DAY_OF_MONTH, new On(nullFieldConstraints, String.format("%sW", dayOfMonth))));
        assertEquals(String.format("at %s:%s the nearest weekday to the %s of the month", hour, minute, dayOfMonth), descriptor.describe(new Cron(results)));
    }
}