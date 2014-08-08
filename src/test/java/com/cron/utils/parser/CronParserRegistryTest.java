package com.cron.utils.parser;

import com.cron.utils.CronType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CronParserRegistryTest {

    @Test
    public void testRetrieveParserUnix() throws Exception {
        assertNotNull(CronParserRegistry.instance().retrieveParser(CronType.UNIX));
    }

    @Test
    public void testRetrieveParserQuartz() throws Exception {
        assertNotNull(CronParserRegistry.instance().retrieveParser(CronType.QUARTZ));
    }

    @Test
    public void testRetrieveParserCron4j() throws Exception {
        assertNotNull(CronParserRegistry.instance().retrieveParser(CronType.CRON4J));
    }

    @Test
    public void testInstance() throws Exception {
        assertNotNull(CronParserRegistry.instance());
        assertEquals(CronParserRegistry.class, CronParserRegistry.instance().getClass());
    }
}