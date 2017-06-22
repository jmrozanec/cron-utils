package com.cronutils.parser;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;

public class CronParserCron4JIntegrationTest {
    private CronParser cron4jParser;

    @Before
    public void setUp() throws Exception {
        CronDefinition cronDefinition =  CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);
        cron4jParser = new CronParser(cronDefinition);
    }

    @Test
    public void testParseIssue32Expression01() throws Exception {
        String cronExpr = "* 1,2,3,4,5,6 * 1,2,3 *";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseIssue32Expression02() throws Exception {
        String cronExpr = "* 1 1,2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseStrictRangeEnforced01() throws Exception {
        String cronExpr = "* 1 1-2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStrictRangeEnforced02() throws Exception {
        String cronExpr = "* 1 5-2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseLastDayOfMonth() throws Exception {
        String cronExpr = "* * L * *";
    }

    //@Test TODO issue 202
    public void testParseSunday() throws Exception {
        String cronExpr = "* * * * sun";
        cron4jParser.parse(cronExpr);
    }
}
