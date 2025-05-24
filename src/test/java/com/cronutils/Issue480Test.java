package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.cronutils.model.field.expression.QuestionMark.questionMark;
import static com.cronutils.model.field.expression.FieldExpressionFactory.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static org.junit.jupiter.api.Assertions.*;

public class Issue480Test {

    private static final Logger LOGGER = LoggerFactory.getLogger(Issue480Test.class);

    private static final CronDefinition definition = CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(1).supportsQuestionMark().and()
            .withDayOfMonth().supportsL().supportsQuestionMark().and()
            .withDayOfYear().supportsQuestionMark().and()
            .withMonth().and()
            .withYear().optional().withValidRange(1970, 2099).and()
            .matchDayOfWeekAndDayOfMonth()
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
            .instance();

    @Test
    public void testIntervalsEvery5thMonthsSinceASpecificMonth() {
        LocalDateTime sunday = LocalDateTime.of(2021, 6, 27, 0, 0);
        assertEquals(sunday.getDayOfWeek(), DayOfWeek.SUNDAY);
        Clock clock = Clock.fixed(sunday.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now(clock);
        LOGGER.info("Now: {}", now);

        Cron cron = getWeekly(now).instance();
        ZonedDateTime nextRun;

        final ZonedDateTime nowPlusWeek = now.plusWeeks(1);
        LOGGER.info("now + 1 week: {}", nowPlusWeek);

        nextRun = nextRun(cron, now); // first run
        LOGGER.info("nextRun: {}", nextRun);

        assertTrue(nextRun.truncatedTo(ChronoUnit.MINUTES)
                .isEqual(nowPlusWeek.truncatedTo(ChronoUnit.MINUTES)));
    }

    private CronBuilder getWeekly(ZonedDateTime now) {
        return CronBuilder.cron(definition)
                .withMinute(on(now.getMinute()))
                .withHour(on(now.getHour()))
//                .withDoW(on(now.getDayOfWeek().ordinal())) // ordinal -- 0 to 6, This is a wrong way of mapping
                .withDoW(on(now.getDayOfWeek().getValue())) // value -- 1 to 7
                .withDoM(questionMark())
                .withDoY(questionMark())
                .withMonth(always())
                .withYear(always());
    }

    private static ZonedDateTime nextRun(Cron cron, ZonedDateTime when) {
        final Optional<ZonedDateTime> next = ExecutionTime.forCron(cron).nextExecution(when);
        if (!next.isPresent()) {
            fail();
        }
        LOGGER.info("Calculated next run at {}", next.get());
        return next.get();
    }

}
