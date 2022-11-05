package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class Issue539Test {

    private final CronParser parser = new CronParser(CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth().supportsL().and()
            .withMonth().and()
            .withDayOfWeek().and()
            .instance());

    @Test
    public void testDOMWithListIncludingLastOfMonth() {
        Cron cron = parser.parse("0 0 L,15 * *").validate();

        ZonedDateTime thirdOfMonth = ZonedDateTime.parse("2022-04-03T00:00:00+00:00");
        ZonedDateTime fifteenthOfMonth = ZonedDateTime.parse("2022-04-15T00:00:00+00:00");
        ZonedDateTime twentiethOfMonth = ZonedDateTime.parse("2022-04-20T00:00:00+00:00");
        ZonedDateTime thirtiethOfMonth = ZonedDateTime.parse("2022-04-30T00:00:00+00:00");


        assertEquals(ExecutionTime.forCron(cron).nextExecution(thirdOfMonth).get(), fifteenthOfMonth);

        // Fails
        assertEquals(ExecutionTime.forCron(cron).nextExecution(twentiethOfMonth).get(), thirtiethOfMonth);
    }

    @Test
    public void testDOMWithLastOfMonth() {
        Cron cron = parser.parse("0 0 L * *").validate();

        ZonedDateTime thirdOfMonth = ZonedDateTime.parse("2022-04-03T00:00:00+00:00");
        ZonedDateTime twentiethOfMonth = ZonedDateTime.parse("2022-04-20T00:00:00+00:00");
        ZonedDateTime thirtiethOfMonth = ZonedDateTime.parse("2022-04-30T00:00:00+00:00");


        assertEquals(ExecutionTime.forCron(cron).nextExecution(thirdOfMonth).get(), thirtiethOfMonth);
        assertEquals(ExecutionTime.forCron(cron).nextExecution(twentiethOfMonth).get(), thirtiethOfMonth);
    }

}
