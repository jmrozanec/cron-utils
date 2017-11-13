package com.cronutils;

import java.time.ZonedDateTime;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue223Test {
    /**
     * Issue #223: for dayOfWeek value == 3 && division of day, nextExecution do not return correct results
     */
    @Test
    public void testEveryWednesdayOfEveryDayNextExecution() {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron myCron = parser.parse("* * * * 3");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-05T11:31:55.407-05:00");
        assertEquals(ZonedDateTime.parse("2017-09-06T00:00-05:00"), ExecutionTime.forCron(myCron).nextExecution(time).get());

        Cron myCron2 = parser.parse("* * */1 * 3");
        time = ZonedDateTime.parse("2017-09-05T11:31:55.407-05:00");
        assertEquals(ZonedDateTime.parse("2017-09-06T00:00-05:00"), ExecutionTime.forCron(myCron2).nextExecution
                (time).get());
    }

}
