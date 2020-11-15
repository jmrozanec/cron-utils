package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


public class Issue403Test {
    @Ignore
    @Test
    public void testCase1() {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        CronDescriptor cd = CronDescriptor.instance(Locale.US);
        assertEquals("every minute every 2 hours", cd.describe(parser.parse("0 * 0/2 * * ?")));
        assertEquals("every minute every hour every 2 days", cd.describe(parser.parse("0 * * 1/2 * ?")));
    }

    @Test
    public void testCase2(){
        CronDefinition defaultDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronDescriptor descriptor = CronDescriptor.instance(Locale.CHINESE);
        CronParser parser = new CronParser(defaultDef);

        String str = "0 * 0/2 * * ?";

        ZonedDateTime now = ZonedDateTime.of(2019, 10, 30, 18, 8, 50, 0, ZoneId.of("US/Central"));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(str));
        
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        assertEquals("2019-10-30 18:09:00", nextExecution.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Test
    public void testCase3(){
        CronDefinition defaultDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronDescriptor descriptor = CronDescriptor.instance(Locale.CHINESE);
        CronParser parser = new CronParser(defaultDef);

        String str = "0 0 0/2 * * ?";

        ZonedDateTime now = ZonedDateTime.of(2019, 10, 30, 18, 8, 50, 0, ZoneId.of("US/Central"));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(str));
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        assertEquals("2019-10-30 20:00:00", nextExecution.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
