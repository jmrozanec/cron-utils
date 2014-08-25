package com.cronutils.mapper;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionRegistry;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CronMapperIntegrationTest {

    @Test
    public void testSpecificTimeCron4jToQuartz(){
        String expression = "30 8 10 6 *";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testMoreThanOneInstanceCron4jToQuartz(){
        String expression = "0 11,16 * * *";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeCron4jToQuartz(){
        String expression = "0 9-18 * * 1-3";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testSpecificTimeQuartzToCron4j(){
        String expected = "30 8 10 6 *";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testMoreThanOneInstanceQuartzToCron4j(){
        String expected = "0 11,16 * * *";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeQuartzToCron4j(){
        String expected = "0 9-18 * * 1-3";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    private CronParser cron4jParser(){
        return new CronParser(CronDefinitionRegistry.instance().retrieve(CronType.CRON4J));
    }

    private CronParser quartzParser(){
        return new CronParser(CronDefinitionRegistry.instance().retrieve(CronType.QUARTZ));
    }

    private CronMapper createFromCron4jToQuartz(){
        return new CronMapper(
                CronDefinitionRegistry.instance().retrieve(CronType.CRON4J),
                CronDefinitionRegistry.instance().retrieve(CronType.QUARTZ));
    }

    private CronMapper createFromQuartzToCron4j(){
        return new CronMapper(
                CronDefinitionRegistry.instance().retrieve(CronType.QUARTZ),
                CronDefinitionRegistry.instance().retrieve(CronType.CRON4J));
    }
}
