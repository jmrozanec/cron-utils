package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class Issue402Test {
    @Test
    public void test() {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        final Cron cron = parser.parse("0 0 0 1 3 ? 2001/2");
//        CronDescriptor cd = CronDescriptor.instance(Locale.US);
//        System.out.println(cd.describe(cron));

        ExecutionTime execution = ExecutionTime.forCron(cron);

        LocalDate date = LocalDate.of(2015, 1, 15);
        ZonedDateTime currentDateTime = ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        Optional<ZonedDateTime> nextExecution = execution.nextExecution(currentDateTime);
        assertTrue(nextExecution.isPresent());
        assertEquals(ZonedDateTime.of(LocalDate.of(2015, 3, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC), nextExecution.get());

        Optional<ZonedDateTime> lastExecution = execution.lastExecution(currentDateTime);
        assertTrue(lastExecution.isPresent());
        assertEquals(ZonedDateTime.of(LocalDate.of(2013, 3, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC), lastExecution.get());

        lastExecution = execution.lastExecution(nextExecution.get());
        assertTrue(lastExecution.isPresent());
        assertEquals(ZonedDateTime.of(LocalDate.of(2013, 3, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC), lastExecution.get());
    }
}
