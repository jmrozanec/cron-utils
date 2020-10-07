package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.*;
import java.util.Optional;

import static com.cronutils.model.field.expression.FieldExpression.questionMark;
import static com.cronutils.model.field.expression.FieldExpressionFactory.every;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static junit.framework.TestCase.fail;

@Ignore
public class Issue446Test {
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
    public void testWrongIntervalsForEvery6Months() {
        LocalDateTime dayOfApril = LocalDateTime.of(2020, 4, 25, 0, 0);
        Clock clock = Clock.fixed(dayOfApril.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        ZonedDateTime dayOfAprilInLocalTimezone = ZonedDateTime.now(clock);
        System.out.println("now: " + dayOfAprilInLocalTimezone);
        Cron cron = getEveryMonthFromNow(dayOfAprilInLocalTimezone, 6).instance();

        ZonedDateTime nextRun = nextRun(cron, dayOfAprilInLocalTimezone); // first run
        Assert.assertEquals(2020, nextRun.getYear());
        Assert.assertEquals(10, nextRun.getMonthValue());

        nextRun = nextRun(cron, nextRun); // second
        System.out.println(nextRun);
        Assert.assertEquals(2021, nextRun.getYear());
        Assert.assertEquals(4, nextRun.getMonthValue());
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
