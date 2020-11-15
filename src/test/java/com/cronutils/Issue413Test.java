package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue413Test {
    @Test
    public void testFridayToSaturdayUnix() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        try {
            parser.parse("* * * */12 *");
        } catch (IllegalArgumentException expected) {
            assertEquals("Failed to parse '* * * */12 *'. Period 12 not in range [1, 12]", expected.getMessage());
        }
    }
}
