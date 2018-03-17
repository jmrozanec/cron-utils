package com.cronutils.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.builder.CronBuilder;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.parser.CronParser;

import static com.cronutils.model.field.expression.FieldExpression.questionMark;
import static com.cronutils.model.field.expression.FieldExpressionFactory.every;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
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

        // CronParser parser = new CronParser(cronDefinition);
        // Cron quartzCron = parser.parse("0 0 0 15 8 ? 2015/2");
        Cron cron1 = parser.parse("0 0 0 15 8 ? 2015/2");
        Cron cron2 = parser.parse("0 0 0 16 9 ? 2015/2");
        Cron cron3 = parser.parse("0 0 0 17 10 ? 2015/2");
        List<Cron> crons = new ArrayList<>();
        crons.add(cron1);
        crons.add(cron2);
        this.cron1 = new CompositeCron(crons);
        List<Cron> crons2 = new ArrayList<>();
        crons.add(cron2);
        crons.add(cron3);
        this.cron2 = new CompositeCron(crons2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weDoNotSupportCronsWithDifferentDefinitions() throws Exception {
        CronDefinition definition2 = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(definition1);
        CronParser parser2 = new CronParser(definition2);

        // CronParser parser = new CronParser(cronDefinition);
        // Cron quartzCron = parser.parse("0 0 0 15 8 ? 2015/2");
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
        cron2.validate();
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
}