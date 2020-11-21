package com.cronutils.parser;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.support.CronExpression;

public class CronParserSpringIntegrationTest {
    private CronParser parser;
    @Before
    public void setUp() {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
    }

    @Test
    public void testSpringCronSupportNthDayOfWeek(){
        CronExpression.parse("0 0 0 ? * WED#2");
        parser.parse("0 0 0 ? * WED#2");
    }

}
