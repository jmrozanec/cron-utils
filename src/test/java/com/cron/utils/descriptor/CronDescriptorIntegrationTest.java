package com.cron.utils.descriptor;

import com.cron.utils.CronType;
import com.cron.utils.parser.CronDefinitionRegistry;
import com.cron.utils.parser.CronParser;
import com.cron.utils.parser.field.FieldConstraints;
import com.cron.utils.parser.field.FieldConstraintsBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorIntegrationTest {

    private CronDescriptor descriptor;
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        parser = new CronParser(CronDefinitionRegistry.instance().retrieve(CronType.QUARTZ));
    }

    @Test
    public void testCronWithAndHours(){
        assertEquals("at 1, 2, 3 and 4 hours", descriptor.describe(parser.parse("* * 1,2,3,4 * * * *")));
    }

    @Test
    public void testCronAndWithRangeHours(){
        assertEquals("at 1, 2, 3 and 4 hours and every hours between 6 and 9 hours",
                descriptor.describe(parser.parse("* * 1,2,3,4,6-9 * * * *")));
    }

    @Test
    public void testCronAndWithRangesAndEveryExpressions(){
        assertEquals("at 0 seconds every 3 minutes between 2 and 59 at 1, 9 " +
                "and 22 hours every day between 11 and 26 every month between January and June",
                descriptor.describe(parser.parse("0 2-59/3 1,9,22 11-26 1-6 ?")));
    }
}
