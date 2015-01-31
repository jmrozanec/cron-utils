package com.cronutils.utils.descriptor;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorCron4jIntegrationTest {
        private CronDescriptor descriptor;
        private CronParser parser;

        @Before
        public void setUp() throws Exception {
            descriptor = CronDescriptor.instance(Locale.UK);
            parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
        }

        @Test
        public void testEveryMinuteBetween1100And1110(){
            assertEquals("every minute between 11:00 and 11:10", descriptor.describe(parser.parse("0-10 11 * * *")));
        }

        @Test
        public void testEveryMinute(){
            assertEquals("every minute", descriptor.describe(parser.parse("* * * * *")));
            assertEquals("every minute", descriptor.describe(parser.parse("*/1 * * * *")));
            assertEquals("every minute", descriptor.describe(parser.parse("0/1 * * * ?")));
        }

    @Test
    public void testEveryFiveMinutes(){
        assertEquals("every 5 minutes", descriptor.describe(parser.parse("*/5 * * * *")));
        assertEquals("every 5 minutes", descriptor.describe(parser.parse("0/5 * * * ?")));
    }

}
