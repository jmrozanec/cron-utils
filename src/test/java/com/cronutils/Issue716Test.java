package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #716: bare {@code L} in day-of-week means every Saturday ("7"/"SAT"),
 * not the last occurrence of a weekday, and must not depend on the definition's
 * day-of-week numbering.
 */
class Issue716Test {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZonedDateTime START = ZonedDateTime.of(2025, 6, 1, 0, 0, 0, 0, UTC);

    @Test
    void bareLQuartzDescriptionIsEverySaturday() {
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
        CronParser quartz = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        String description = descriptor.describe(quartz.parse("0 0 0 ? * L *"));
        assertEquals("at 00:00 every Saturday", description);
    }

    @Test
    void bareLSpring53DescriptionIsEverySaturday() {
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
        CronParser spring53 = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53));
        String description = descriptor.describe(spring53.parse("0 0 0 ? * L"));
        assertEquals("at 00:00 every Saturday", description);
    }

    @Test
    void bareLQuartzSchedulesEverySaturday() {
        CronParser quartz = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        List<ZonedDateTime> next = nextExecutions(ExecutionTime.forCron(quartz.parse("0 0 0 ? * L *")), START, 5);
        assertEquals(saturdayDates(), next);
        next.forEach(zdt -> assertEquals(DayOfWeek.SATURDAY, zdt.getDayOfWeek()));
    }

    @Test
    void bareLSpring53SchedulesEverySaturday() {
        CronParser spring53 = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53));
        List<ZonedDateTime> next = nextExecutions(ExecutionTime.forCron(spring53.parse("0 0 0 ? * L")), START, 5);
        assertEquals(saturdayDates(), next);
        next.forEach(zdt -> assertEquals(DayOfWeek.SATURDAY, zdt.getDayOfWeek()));
    }

    @Test
    void nLStillMeansLastWeekdayOfMonth() {
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
        CronParser quartz = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        // Quartz: 6 = Friday; 6L = last Friday of every month
        String description = descriptor.describe(quartz.parse("0 15 10 ? * 6L *"));
        assertTrue(description.contains("last Friday of every month"), description);
    }

    private static List<ZonedDateTime> saturdayDates() {
        List<ZonedDateTime> expected = new ArrayList<>();
        expected.add(ZonedDateTime.of(2025, 6, 7, 0, 0, 0, 0, UTC));
        expected.add(ZonedDateTime.of(2025, 6, 14, 0, 0, 0, 0, UTC));
        expected.add(ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, UTC));
        expected.add(ZonedDateTime.of(2025, 6, 28, 0, 0, 0, 0, UTC));
        expected.add(ZonedDateTime.of(2025, 7, 5, 0, 0, 0, 0, UTC));
        return expected;
    }

    private static List<ZonedDateTime> nextExecutions(ExecutionTime executionTime, ZonedDateTime start, int count) {
        List<ZonedDateTime> result = new ArrayList<>();
        ZonedDateTime cursor = start;
        for (int i = 0; i < count; i++) {
            Optional<ZonedDateTime> next = executionTime.nextExecution(cursor);
            assertTrue(next.isPresent());
            cursor = next.get();
            result.add(cursor);
        }
        return result;
    }
}
