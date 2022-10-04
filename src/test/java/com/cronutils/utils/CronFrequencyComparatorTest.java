package com.cronutils.utils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CronFrequencyComparatorTest {
    private CronFrequencyComparator comparator;
    private CronParser parser;
    private Cron cron1;
    private Cron cron2;

    @BeforeEach
    public void setUp() {
        ZonedDateTime date1 = LocalDateTime.of(2018, 11, 5, 0, 0, 0).atZone(ZoneId.of("UTC"));
        ZonedDateTime date2 = LocalDateTime.of(2018, 11, 11, 0, 0, 0).atZone(ZoneId.of("UTC"));
        comparator = new CronFrequencyComparator(date1, date2);
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        parser = new CronParser(cronDefinition);

        cron1 = parser.parse("0 0 9-17 * * MON-FRI");//on the hour nine-to-five weekdays -> 9 executions per day, five times a week -> 45 executions per week
        cron2 = parser.parse("0 0/30 8-10 * * *");//8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day -> six executions per day, seven times a week -> 42 executions per week
    }

    @Test
    public void compareMoreFrequent() {
        assertTrue(comparator.compare(cron1, cron2)>0);
    }

    @Test
    public void compareLessFrequent() {
        assertTrue(comparator.compare(cron2, cron1)<0);
    }

    @Test
    public void compareEqualFrequent() {
        Cron cronx = parser.parse("0 0 9-17 * * MON-FRI");//on the hour nine-to-five weekdays -> 9 executions per day, five times a week -> 45 executions per week
        assertEquals(0, comparator.compare(cron1, cronx));
    }
}
