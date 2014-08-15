package com.cron.utils.model;

import org.junit.Test;

public class CronDefinitionTest {

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new CronDefinition(null, false);
    }

}