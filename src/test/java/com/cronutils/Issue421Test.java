package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Locale;
import java.util.Optional;

import static com.cronutils.model.field.expression.QuestionMark.questionMark;
import static com.cronutils.model.field.expression.FieldExpressionFactory.every;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static org.junit.jupiter.api.Assertions.*;

public class Issue421Test {

    private static final CronDefinition definition = CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfWeek().supportsQuestionMark().and()
            .withDayOfMonth().supportsL().supportsQuestionMark().and()
            .withDayOfYear().supportsQuestionMark().and()
            .withMonth().and()
            .matchDayOfWeekAndDayOfMonth()
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
            .instance();

    @Test
    public void testIntervalsEvery5thMonthsSinceASpecificMonth() {
        LocalDateTime firstOfJanuary = LocalDateTime.of(2020, 2, 10, 0, 0);
        Clock clock = Clock.fixed(firstOfJanuary.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now(clock);
        System.out.println("now: " + now);

        Cron cron = getEveryMonthFromNow(now, 5).instance();
        ZonedDateTime nextRun;

        nextRun = nextRun(cron, now); // first run
        assertEquals(2020, nextRun.getYear());
        assertEquals(7, nextRun.getMonthValue());

        nextRun = nextRun(cron, nextRun); // first run
        assertEquals(2020, nextRun.getYear());
        assertEquals(12, nextRun.getMonthValue());

        nextRun = nextRun(cron, nextRun); // first run
        assertEquals(2021, nextRun.getYear());
        assertEquals(2, nextRun.getMonthValue());
    }

    @Test
    public void testIntervalsEvery5thMonth() {
        LocalDateTime firstOfJanuary = LocalDateTime.of(2020, 2, 10, 0, 0);
        Clock clock = Clock.fixed(firstOfJanuary.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now(clock);
        System.out.println("now: " + now);

        Cron cron = getEveryMonth(now, 5).instance();
        ZonedDateTime nextRun;

        nextRun = nextRun(cron, now); // first run
        assertEquals(2020, nextRun.getYear());
        assertEquals(6, nextRun.getMonthValue());

        nextRun = nextRun(cron, nextRun); // first run
        assertEquals(2020, nextRun.getYear());
        assertEquals(11, nextRun.getMonthValue());

        nextRun = nextRun(cron, nextRun); // first run
        assertEquals(2021, nextRun.getYear());
        assertEquals(1, nextRun.getMonthValue());
    }

    @Test
    public void testDescriptionEveryXMonths() {
        ZonedDateTime now = ZonedDateTime.now();

        String description = CronDescriptor.instance(Locale.US).describe(getEveryMonth(now, 3).instance());
        System.out.println(description);
        assertTrue(description.contains("every 3 months"));

        description = CronDescriptor.instance(Locale.US).describe(getEveryMonth(now, 6).instance());
        System.out.println(description);
        assertTrue(description.contains("every 6 months"));
    }

    public static CronBuilder getEveryMonth(ZonedDateTime now, int every) {
        return CronBuilder.cron(definition)
                .withMinute(on(now.getMinute()))
                .withHour(on(now.getHour()))
                .withDoW(questionMark())
                .withDoM(on(now.getDayOfMonth()))
                .withDoY(questionMark())
                .withMonth(every(every));
    }

    public static CronBuilder getEveryMonthFromNow(ZonedDateTime now, int every) {
        return CronBuilder.cron(definition)
                .withMinute(on(now.getMinute()))
                .withHour(on(now.getHour()))
                .withDoW(questionMark())
                .withDoM(on(now.getDayOfMonth()))
                .withDoY(questionMark())
                .withMonth(every(on(now.getMonthValue()), every));
    }

    private static ZonedDateTime nextRun(Cron cron, ZonedDateTime when) {
        final Optional<ZonedDateTime> next = ExecutionTime.forCron(cron).nextExecution(when);
        if (!next.isPresent()) {
            fail();
        }
        System.out.println("Calculated next run at " + next.get());
        return next.get();
    }

}
