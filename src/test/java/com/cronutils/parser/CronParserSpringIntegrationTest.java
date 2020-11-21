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

    /**
     * The example is adapted from: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html
     */
    @Test
    public void testSpringCronSupportNthDayOfWeek(){
        CronExpression.parse("0 0 0 ? * FRI#1");
        parser.parse("0 0 0 ? * FRI#1");
    }

}
