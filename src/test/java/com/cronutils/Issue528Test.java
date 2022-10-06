package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

public class Issue528Test {
    private static final CronDefinition REBOOT_CRON_DEFINITION = CronDefinitionBuilder.defineCron()
            .withSupportedNicknameReboot()
            .instance();

    @Test
    public void testRebootExecutionTime() {
        Cron cron = new CronParser(REBOOT_CRON_DEFINITION).parse("@reboot");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Assertions.assertEquals(Optional.empty(), executionTime.nextExecution(ZonedDateTime.now()));
        Assertions.assertEquals(Optional.empty(), executionTime.lastExecution(ZonedDateTime.now()));
    }

    @Test
    public void testCronDescriptor() {
        Cron cron = new CronParser(REBOOT_CRON_DEFINITION).parse("@reboot");
        String description = CronDescriptor.instance(Locale.UK).describe(cron);
        Assertions.assertEquals("on reboot", description);
    }

    @Test
    public void testCronMapperRebootSupportedOnTarget() {
        Cron cron = new CronParser(REBOOT_CRON_DEFINITION).parse("@reboot");
        CronDefinition unix = CronDefinitionBuilder.defineCron()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).withStrictRange().and()
                .withMonth().withValidRange(1, 12).withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).withStrictRange().and()
                .withSupportedNicknameReboot()
                .instance();
        Cron mapped = CronMapper.sameCron(unix).map(cron);
        Assertions.assertEquals(cron.asString(), mapped.asString());
    }

    @Test
    public void testCronMapperRebootNotSupportedOnTarget() {
        Cron cron = new CronParser(REBOOT_CRON_DEFINITION).parse("@reboot");
        CronDefinition unix = CronDefinitionBuilder.defineCron()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).withStrictRange().and()
                .withMonth().withValidRange(1, 12).withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).withStrictRange().and()
                .instance();
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronMapper.sameCron(unix).map(cron));
    }
}
