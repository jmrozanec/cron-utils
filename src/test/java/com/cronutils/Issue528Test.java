package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

class Issue528Test {

    static final CronDefinition REBOOT_CRON_DEFINITION = CronDefinitionBuilder.defineCron()
            .withSupportedNicknameReboot()
            .instance();

    @Test
    void testRebootExecutionTime() {
        Cron cron = new CronParser(REBOOT_CRON_DEFINITION).parse("@reboot");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Assertions.assertEquals(Optional.empty(), executionTime.nextExecution(ZonedDateTime.now()));
        Assertions.assertEquals(Optional.empty(), executionTime.lastExecution(ZonedDateTime.now()));
    }
}
