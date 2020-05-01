package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

import static org.junit.Assert.assertEquals;

@Ignore
public class Issue424Test {
    @Test
    public void test() {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        ExecutionTime execution = ExecutionTime.forCron(parser.parse("0 0 12 ? * SUN#4 2020"));
        LocalDate date = LocalDate.of(2021, 1, 1);
        LocalTime time = LocalTime.of(0, 0, 0);
        ZonedDateTime dateTime = ZonedDateTime.of(date, time, ZoneOffset.UTC);
        for (int index = 0, size = 12; index < size; index++) {
            dateTime = execution.lastExecution(dateTime).orElse(null);
            assertEquals(LocalDateTime.of(2020, 12 - index, 1, 12, 0, 0).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SUNDAY)), dateTime.toLocalDateTime());
        }
    }
}
