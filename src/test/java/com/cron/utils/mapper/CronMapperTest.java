package com.cron.utils.mapper;

import com.cron.utils.model.CronDefinition;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class CronMapperTest {
    private CronMapper mapper;

    @Test(expected = NullPointerException.class)
    public void testConstructorSourceDefinitionNull() throws Exception {
        new CronMapper(mock(CronDefinition.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorTargetDefinitionNull() throws Exception {
        new CronMapper(null, mock(CronDefinition.class));
    }
}