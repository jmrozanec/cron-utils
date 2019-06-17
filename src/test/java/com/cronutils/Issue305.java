package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class Issue305 {

    @Test
    public void testIssue305(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("0 0 0 15 8 ? 2015-2099/2");

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")));
        Set<ZonedDateTime> dates = new LinkedHashSet<>();
        while (!nextExecution.get().isAfter(ZonedDateTime.of(2020, 12, 31, 0, 0, 0, 0, ZoneId.of("UTC")))) {
            dates.add(nextExecution.get());
            nextExecution = executionTime.nextExecution(nextExecution.get());
        }
        Set<Integer> years = dates.stream().map(d->d.getYear()).collect(Collectors.toSet());
        Set<Integer> expectedYears = new HashSet<>();
        expectedYears.add(2015);
        expectedYears.add(2017);
        expectedYears.add(2019);
        assertEquals(expectedYears, years);
    }
}
