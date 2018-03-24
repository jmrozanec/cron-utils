package com.cronutils.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CompositeCronTest {
    private CronDefinition definition1;
    private Cron cron1;
    private Cron cron2;

    @Before
    public void setUp(){
        definition1 = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(definition1);

        Cron cron1 = parser.parse("0 0 0 15 8 ? 2015/2");
        Cron cron2 = parser.parse("0 0 0 16 9 ? 2015/2");
        Cron cron3 = parser.parse("0 0 0 17 10 ? 2015/2");
        List<Cron> crons = new ArrayList<>();
        crons.add(cron1);
        crons.add(cron2);
        this.cron1 = new CompositeCron(crons);
        List<Cron> crons2 = new ArrayList<>();
        crons2.add(cron2);
        crons2.add(cron3);
        this.cron2 = new CompositeCron(crons2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weDoNotSupportCronsWithDifferentDefinitions() throws Exception {
        CronDefinition definition2 = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(definition1);
        CronParser parser2 = new CronParser(definition2);

        Cron cron1 = parser.parse("0 0 0 15 8 ? 2015/2");
        Cron cron2 = parser2.parse("0 0 0 * *");
        List<Cron> crons = new ArrayList<>();
        crons.add(cron1);
        crons.add(cron2);
        new CompositeCron(crons);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weDoNotSupportCompositeWithoutCrons() throws Exception {
        new CompositeCron(new ArrayList<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void retrieve() throws Exception {
        cron1.retrieve(CronFieldName.DAY_OF_WEEK);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void retrieveFieldsAsMap() throws Exception {
        cron1.retrieveFieldsAsMap();
    }

    @Test
    public void asString() throws Exception {
        assertEquals("0 0 0 15|16 8|9 ? 2015/2", cron1.asString());
    }

    @Test
    public void getCronDefinition() throws Exception {
        assertEquals(definition1, cron1.getCronDefinition());
    }

    @Test
    public void validate() throws Exception {
        cron1.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateThrowsExceptionEmptyCrons(){
        new CompositeCron(new ArrayList<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void equivalent() throws Exception {
        cron1.equivalent(CronMapper.fromQuartzToCron4j(), mock(Cron.class));
    }

    @Test
    public void equivalent1() throws Exception {
        assertTrue(cron1.equivalent(cron1));
        assertFalse(cron1.equivalent(cron2));
    }

    @Test
    public void testExampleIssue318(){
        CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(definition);
        Cron cron1 = parser.parse("0 0 9 * * ? *");
        Cron cron2 = parser.parse("0 0 10 * * ? *");
        Cron cron3 = parser.parse("0 30 11 * * ? *");
        Cron cron4 = parser.parse("0 0 12 * * ? *");

        List<Cron> crons = new ArrayList<>();
        crons.add(cron1);
        crons.add(cron2);
        crons.add(cron3);
        crons.add(cron4);
        Cron composite = new CompositeCron(crons);

        ZonedDateTime defaultt = ZonedDateTime.of(2000, 4, 15, 0, 0, 0, 0, UTC);

        assertEquals("0 0|0|30|0 9|10|11|12 * * ? *", composite.asString());
        ExecutionTime executionTime = ExecutionTime.forCron(composite);
        ZonedDateTime date1 = ZonedDateTime.of(2015, 4, 15, 0, 0, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2015, 4, 15, 9, 0, 0, 0, UTC), executionTime.nextExecution(date1).orElse(defaultt));
        ZonedDateTime date2 = ZonedDateTime.of(2015, 4, 15, 9, 30, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2015, 4, 15, 10, 0, 0, 0, UTC), executionTime.nextExecution(date2).orElse(defaultt));
        ZonedDateTime date3 = ZonedDateTime.of(2015, 4, 15, 11, 0, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2015, 4, 15, 11, 30, 0, 0, UTC), executionTime.nextExecution(date3).orElse(defaultt));
        ZonedDateTime date4 = ZonedDateTime.of(2015, 4, 15, 11, 30, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2015, 4, 15, 12, 0, 0, 0, UTC), executionTime.nextExecution(date4).orElse(defaultt));
        ZonedDateTime date5 = ZonedDateTime.of(2015, 4, 15, 12, 30, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2015, 4, 16, 9, 0, 0, 0, UTC), executionTime.nextExecution(date5).orElse(defaultt));
    }

    @Test
    public void testIssue263(){
        String multicron = "0 1 0 ? 1/1 MON#2|MON#3|MON#4|MON#5 *";
        CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(definition);
        Cron cron = parser.parse(multicron);
        assertEquals(multicron.replaceAll("MON", "2"), cron.asString());
    }
}