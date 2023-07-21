package com.cronutils;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Issue605Test {

    private CronParser cronParser;

    @BeforeEach
    public void setUp() {
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
    }

    @Test
    public void testDayOfWeekMapping() {
        Cron cron = this.cronParser.parse("0 0 0 ? * 5#1");
        CronMapper mapper = CronMapper.fromSpringToQuartz();
        assertDoesNotThrow(() -> mapper.map(cron));
    }

}
