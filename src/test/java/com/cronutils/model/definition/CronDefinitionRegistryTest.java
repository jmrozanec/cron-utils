package com.cronutils.model.definition;

import com.cronutils.model.CronType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CronDefinitionRegistryTest {

    @Test
    public void testRetrieveParserUnix() throws Exception {
        assertNotNull(CronDefinitionRegistry.instance().retrieve(CronType.UNIX));
    }

    @Test
    public void testRetrieveParserQuartz() throws Exception {
        assertNotNull(CronDefinitionRegistry.instance().retrieve(CronType.QUARTZ));
    }

    @Test
    public void testRetrieveParserCron4j() throws Exception {
        assertNotNull(CronDefinitionRegistry.instance().retrieve(CronType.CRON4J));
    }

    @Test
    public void testInstance() throws Exception {
        assertNotNull(CronDefinitionRegistry.instance());
        assertEquals(CronDefinitionRegistry.class, CronDefinitionRegistry.instance().getClass());
    }
}