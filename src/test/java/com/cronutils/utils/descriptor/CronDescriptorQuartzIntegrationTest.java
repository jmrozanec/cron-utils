package com.cronutils.utils.descriptor;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorQuartzIntegrationTest {

    private CronDescriptor descriptor;
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testCronWithAndHours(){
        assertEquals("at 1, 2, 3 and 4 hours", descriptor.describe(parser.parse("* * 1,2,3,4 * * * *")));
    }

    @Test
    public void testCronAndWithRangeHours(){
        assertEquals("at 1, 2, 3 and 4 hours and every hour between 6 and 9",
                descriptor.describe(parser.parse("* * 1,2,3,4,6-9 * * * *")));
    }

    @Test
    public void testCronAndWithRangesAndEveryExpressions(){
        assertEquals("every 3 minutes between 2 and 59 at 1, 9 " +
                "and 22 hours every day between 11 and 26 every month between January and June",
                descriptor.describe(parser.parse("0 2-59/3 1,9,22 11-26 1-6 ?")));
    }

    @Test
    public void testEverySecond(){
        assertEquals("every second", descriptor.describe(parser.parse("* * * * * *")));
    }

    @Test
    public void testEvery45Seconds(){
        assertEquals("every 45 seconds", descriptor.describe(parser.parse("*/45 * * * * *")));
    }

    @Test
    public void testEveryHour(){
        assertEquals("every hour", descriptor.describe(parser.parse("0 0 * * * ?")));
        assertEquals("every hour", descriptor.describe(parser.parse("0 0 0/1 * * ?")));
    }
}
