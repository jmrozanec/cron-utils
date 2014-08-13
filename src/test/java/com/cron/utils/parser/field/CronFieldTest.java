package com.cron.utils.parser.field;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CronField.class, CronParser.class})
public class CronFieldTest {

    private CronFieldName testFieldName;
    private FieldParser mockParser;
    private CronFieldExpression mockParseResponse;

    private CronField cronField;

    @Before
    public void setUp() throws Exception {
        testFieldName = CronFieldName.SECOND;
        mockParser = mock(FieldParser.class);
        mockParseResponse = mock(CronFieldExpression.class);

        when(mockParser.parse(anyString())).thenReturn(mockParseResponse);
        PowerMockito.whenNew(FieldParser.class)
                .withArguments(any(FieldConstraints.class)).thenReturn(mockParser);

        cronField = new CronField(testFieldName, mock(FieldConstraints.class));
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(testFieldName, cronField.getField());
    }

    @Test
    public void testParse() throws Exception {
        String cron = UUID.randomUUID().toString();
        CronFieldParseResult result = cronField.parse(cron);
        assertEquals(mockParseResponse, result.getExpression());
        assertEquals(testFieldName, result.getField());
        verify(mockParser).parse(cron);
    }
}
