package com.cronutils.utils.descriptor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

/**
 * @author minidmnv
 */
public class Issue227Test {
    /**
     * Issue #227 - Getting a leaking "%s" in description output
     */
    private CronParser parser;
    private ZonedDateTime currentDateTime;

    @Before
    public void setUp() throws Exception {
        currentDateTime = ZonedDateTime.of(LocalDateTime.of(2016, 12, 20, 12, 00),
                ZoneId.systemDefault());

        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testProperDescriptorOutput() {
        Cron cron = parser.parse("0 5-35/30 * * * ?");
        CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
        String description = descriptor.describe(cron);

        assertEquals("every 30 minutes between 5 and 35", description);
    }

    @Test
    public void testProperDescriptorOutputWithSeconds() {
        Cron cron = parser.parse("5-35/30 * * * * ?");
        CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
        String description = descriptor.describe(cron);

        assertEquals("every 30 seconds between 5 and 35", description);
    }

}
