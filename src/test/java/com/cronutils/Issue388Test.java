package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static org.junit.Assert.assertEquals;


public class Issue388Test {

    //@Test //TODO fix!
    public void testLastAndNextExecutionWithDowAndDom() {

        ZonedDateTime dateTime = ZonedDateTime.of(2019, 07, 01, 8, 0, 0, 0, UTC);

        CronParser springCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
        Cron springCron = springCronParser.parse("0 0 8 1-7 * SAT");
        ExecutionTime springExecutionTime = ExecutionTime.forCron(springCron);
        ZonedDateTime nextSpringExecutionTime = springExecutionTime.nextExecution(dateTime).get();
        ZonedDateTime lastSpringExecutionTime = springExecutionTime.lastExecution(dateTime).get();

        //quartz
        CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        Cron quartzCron = quartzCronParser.parse("0 0 8 ? * SAT#1");
        ExecutionTime quartzExecutionTime = ExecutionTime.forCron(quartzCron);
        ZonedDateTime nextQuartzExecutionTime = quartzExecutionTime.nextExecution(dateTime).get();
        ZonedDateTime lastQuartzExecutionTime = quartzExecutionTime.lastExecution(dateTime).get();

        ZonedDateTime lastMonthFirstSaturday = dateTime.withMonth(6)
                                                       .with(firstInMonth(DayOfWeek.SATURDAY))
                                                       .withHour(8)
                                                       .withMinute(0)
                                                       .withSecond(0)
                                                       .withNano(0);
        ZonedDateTime nextMonthFirstSaturday = dateTime.withMonth(7)
                                                       .with(firstInMonth(DayOfWeek.SATURDAY))
                                                       .withHour(8)
                                                       .withMinute(0)
                                                       .withSecond(0)
                                                       .withNano(0);
        //quartz
        assertEquals(lastMonthFirstSaturday, lastQuartzExecutionTime);
        assertEquals(nextMonthFirstSaturday, nextQuartzExecutionTime);
        //spring
        assertEquals(lastMonthFirstSaturday, lastSpringExecutionTime);
        assertEquals(nextMonthFirstSaturday, nextSpringExecutionTime);

    }
}
