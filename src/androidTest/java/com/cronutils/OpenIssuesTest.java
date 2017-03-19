package com.cronutils;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.text.ParseException;

import org.junit.runner.RunWith;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

@RunWith(AndroidJUnit4.class)
public class OpenIssuesTest {
    private DateTimeFormatter dfSimple = DateTimeFormatter.ofPattern("hh:mm:ss MM/dd/yyyy a X");
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("hh:mm:ss EEE, MMM dd yyyy a X");

    @Test
    public void testBasicCron() throws ParseException {
        printDate("03:15:00 11/20/2015 PM Z");
        printDate("03:15:00 11/27/2015 PM Z");
// printDate("3:15:00 11/29/2015 PM");
// printDate("3:15:00 11/30/2015 PM");
// printDate("3:15:00 12/01/2015 PM");
// printDate("3:15:00 12/02/2015 PM");
// printDate("3:15:00 12/29/2015 PM");
// printDate("3:15:00 12/30/2015 PM");
// printDate("3:15:00 12/31/2015 PM");
    }

    private void printDate(String startDate) throws ParseException {
        ZonedDateTime now = ZonedDateTime.parse(startDate, dfSimple);
        System.out.println("Starting: " + df.format(now));
        printNextDate(now, "0 6 * * 0");//Sunday
        printNextDate(now, "0 6 * * 1");
        printNextDate(now, "0 6 * * 2");
        printNextDate(now, "0 6 * * 3");
        printNextDate(now, "0 6 * * 4");
        printNextDate(now, "0 6 * * 5");
        printNextDate(now, "0 6 * * 6");
    }

    private void printNextDate(ZonedDateTime now, String cronString) {
        ZonedDateTime date = nextSchedule(cronString, now);
        System.out.println("Next time: " + df.format(date));
    }

    private static ZonedDateTime nextSchedule(String cronString, ZonedDateTime lastExecution) {
        CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = cronParser.parse(cronString);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        return executionTime.nextExecution(lastExecution).get();
    }
}

