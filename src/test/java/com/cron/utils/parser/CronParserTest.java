package com.cron.utils.parser;

import com.cron.utils.model.definition.CronDefinition;
import com.cron.utils.model.field.definition.FieldDefinition;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.mockito.Mockito.when;

public class CronParserTest {
    @Mock
    private CronDefinition definition;

    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Set<FieldDefinition> set = Sets.newHashSet();
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse() throws Exception {
        parser.parse("");
    }
}