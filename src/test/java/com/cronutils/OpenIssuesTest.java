package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class OpenIssuesTest {
    /**
     * Issue #79: Next execution skipping valid date:
     */
    public void testNextExecution2014() {
        String crontab = "0 8 * * 1";//m,h,dom,m,dow ; every monday at 8AM
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2014-11-30T00:00:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2014-12-01T08:00:00Z"), executionTime.nextExecution(date));
    }

    /*Issue #58*/

}
