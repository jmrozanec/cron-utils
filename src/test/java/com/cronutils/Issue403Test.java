package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

@Ignore
public class Issue403Test {
    @Test
    public void test() {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        CronDescriptor cd = CronDescriptor.instance(Locale.US);
        assertEquals("every minute every 2 hours", cd.describe(parser.parse("0 * 0/2 * * ?")));
        assertEquals("every minute every hour every 2 days", cd.describe(parser.parse("0 * * 1/2 * ?")));
    }
}
