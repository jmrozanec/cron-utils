package com.cronutils.utils.descriptor;

import static org.hamcrest.MatcherAssert.assertThat;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import org.junit.Ignore;
import org.junit.Test;

@Ignore ("Ignore this until someone is working on a fix")
public class Issue281Test {

    private static final String ISSUE_EXPRESSION = "0 0 0 24 1/12 ?";

    @Test
    public void testCronTypeQuartz() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse(ISSUE_EXPRESSION);
        assertThat("cron is not null", cron != null);
    }
}
