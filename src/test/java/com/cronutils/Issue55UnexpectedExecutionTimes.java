package com.cronutils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraint;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue55UnexpectedExecutionTimes {
    private CronDefinition cronDefinition;

    /**
     * Setup.
     */
    @Before
    public void setUp() {
        cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek()//Monday=1
                .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
                .supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().optional().and()
                .withCronValidation(
                        //both a day-of-week AND a day-of-month parameter should fail for this case; otherwise returned values are correct
                        new CronConstraint("Both, a day-of-week AND a day-of-month parameter, are not supported.") {
                            @Override
                            public boolean validate(Cron cron) {
                                if (!(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark)) {
                                    return cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark;
                                } else {
                                    return !(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark);
                                }
                            }
                        })
                .instance();
    }

    /**
     * Test.
     */
    @Test
    public void testOnceEveryThreeDaysNoInstantsWithinTwoDays() {
        System.out.println();
        System.out.println("TEST1 - expecting 0 instants");
        ZonedDateTime startTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime endTime = startTime.plusDays(2);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 0 */3 * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        List<Instant> instants = getInstants(executionTime, startTime, endTime);
        System.out.println("instants.size() == " + instants.size());
        System.out.println("instants: " + instants);
        assertEquals(0, instants.size());
    }

    /**
     * Test.
     */
    @Test
    public void testOnceAMonthTwelveInstantsInYear() {
        System.out.println();
        System.out.println("TEST2 - expecting 12 instants");
        ZonedDateTime startTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime endTime = startTime.plusYears(1);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 12 L * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        List<Instant> instants = getInstants(executionTime, startTime, endTime);
        System.out.println("instants.size() == " + instants.size());
        System.out.println("instants: " + instants);
        assertEquals(12, instants.size());
    }

    private List<Instant> getInstants(ExecutionTime executionTime, ZonedDateTime startTime, ZonedDateTime endTime) {
        List<Instant> instantList = new ArrayList<>();
        ZonedDateTime next = executionTime.nextExecution(startTime).get();
        while (next.isBefore(endTime)) {
            instantList.add(next.toInstant());
            next = executionTime.nextExecution(next).get();
        }
        return instantList;
    }

}
