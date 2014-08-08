package com.cron.utils.descriptor;

import com.cron.utils.CronParameter;
import com.cron.utils.CronType;
import com.cron.utils.parser.CronParser;
import com.cron.utils.parser.CronParserRegistry;
import com.cron.utils.parser.field.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorTest {

    private CronDescriptor descriptor;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
    }

    @Test
    public void testDescribeEveryXTimeUnits() throws Exception {
        int time = 3;
        Every expression = new Every(null, "" + time);
        assertEquals(String.format("every %s seconds", time), descriptor.describe(
                        Lists.asList(new CronFieldParseResult(CronParameter.SECOND, expression), new CronFieldParseResult[]{})
                )
        );
        assertEquals(String.format("every %s minutes", time), descriptor.describe(
                        Lists.asList(new CronFieldParseResult(CronParameter.MINUTE, expression), new CronFieldParseResult[]{})
                )
        );
        List<CronFieldParseResult> params = Lists.newArrayList();
        params.add(new CronFieldParseResult(CronParameter.HOUR, expression));
        params.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + time)));
        assertEquals(String.format("every %s hours At minute %s", time, time), descriptor.describe(params));
    }

    @Test
    public void testDescribeEveryXMinutesBetweenTime() throws Exception {
        int hour = 11;
        int start = 0;
        int end = 10;
        Between expression = new Between(null, "" + start, "" + end);
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.MINUTE, expression));
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        assertEquals(String.format("every minute between %s:%02d and %s:%02d", hour, start, hour, end), descriptor.describe(results));
    }

    @Test
    public void testDescribeAtXTimeBetweenDaysOfWeek() throws Exception {
        int hour = 11;
        int minute = 30;
        int start = 2;
        int end = 6;
        Between expression = new Between(null, "" + start, "" + end);
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + minute)));
        results.add(new CronFieldParseResult(CronParameter.DAY_OF_WEEK, expression));
        assertEquals(String.format("At %s:%s every day between Tuesday and Saturday", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testDescribeAtXHours() throws Exception {
        int hour = 11;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.SECOND, new Always(null)));
        assertEquals(String.format("At %s:00", hour), descriptor.describe(results));
    }

    @Test
    public void testEverySecondInMonth() throws Exception {
        int month = 2;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.SECOND, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.MONTH, new On(null, "" + month)));
        assertEquals("every second At February month", descriptor.describe(results));
    }

    @Test
    public void testEveryMinuteBetweenMonths() throws Exception {
        int monthStart = 2;
        int monthEnd = 3;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new Always(null)));
        results.add(new CronFieldParseResult(CronParameter.MONTH, new Between(null, "" + monthStart, "" + monthEnd)));
        assertEquals("every minute every month between February and March", descriptor.describe(results));
    }

    @Test
    public void testLastDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + minute)));
        results.add(new CronFieldParseResult(CronParameter.DAY_OF_WEEK, new On(null, String.format("%sL", dayOfWeek))));
        assertEquals(String.format("At %s:%s last Tuesday of every month", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testNthDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + minute)));
        results.add(new CronFieldParseResult(CronParameter.DAY_OF_WEEK, new On(null, String.format("%s#%s", dayOfWeek, dayOfWeek))));
        assertEquals(String.format("At %s:%s Tuesday %s of every month", hour, minute, dayOfWeek), descriptor.describe(results));
    }

    @Test
    public void testLastDayOfMonth() throws Exception {
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + minute)));
        results.add(new CronFieldParseResult(CronParameter.DAY_OF_MONTH, new On(null, "L")));
        assertEquals(String.format("At %s:%s last day of the month", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testNearestWeekdayToNthOfMonth() throws Exception {
        int dayOfMonth = 22;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronParameter.HOUR, new On(null, "" + hour)));
        results.add(new CronFieldParseResult(CronParameter.MINUTE, new On(null, "" + minute)));
        results.add(new CronFieldParseResult(CronParameter.DAY_OF_MONTH, new On(null, String.format("%sW", dayOfMonth))));
        assertEquals(String.format("At %s:%s the nearest weekday to the %s of the month", hour, minute, dayOfMonth), descriptor.describe(results));
    }

    public static void main(String[] args) {
        CronParser parser = CronParserRegistry.instance().retrieveParser(CronType.QUARTZ);

//create a descriptor for a specific Locale
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

//parse some expression and ask descriptor for description
        System.out.println(descriptor.describe(parser.parse("*/45 * * * * *")));;
//description will be: "every 45 seconds"

        for(CronFieldParseResult res : parser.parse("0 23 ? * * 1-5 *")){
            System.out.println(res.getField());
            System.out.println(res.getExpression());
            System.out.println(res.getExpression().getClass());
            System.out.println("****");
        }

        System.out.println(descriptor.describe(parser.parse("0 23 ? * * 1-5 *")));
//description will be: "Every minute 23 every day between Monday and Friday"
    }

}