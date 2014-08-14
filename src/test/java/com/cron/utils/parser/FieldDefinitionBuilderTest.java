package com.cron.utils.parser;

import com.cron.utils.CronFieldName;
import com.cron.utils.model.FieldDefinition;
import com.cron.utils.parser.field.FieldConstraints;
import com.cron.utils.parser.field.FieldConstraintsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FieldConstraintsBuilder.class, FieldDefinitionBuilder.class})
public class FieldDefinitionBuilderTest {
    private CronFieldName testFieldName;
    @Mock
    private CronDefinitionBuilder mockParserBuilder;
    @Mock
    private FieldConstraintsBuilder mockConstraintsBuilder;

    private FieldDefinitionBuilder fieldDefinitionBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;

        when(mockConstraintsBuilder.forField(any(CronFieldName.class))).thenReturn(mockConstraintsBuilder);
        PowerMockito.mockStatic(FieldConstraintsBuilder.class);
        PowerMockito.when(FieldConstraintsBuilder.instance()).thenReturn(mockConstraintsBuilder);

        fieldDefinitionBuilder = new FieldDefinitionBuilder(mockParserBuilder, testFieldName);
    }

    @Test
    public void testWithIntMapping() throws Exception {
        int source = 7;
        int dest = 0;

        fieldDefinitionBuilder.withIntMapping(source, dest);

        verify(mockConstraintsBuilder).withIntValueMapping(source, dest);
    }

    @Test
    public void testAnd() throws Exception {
        FieldConstraints constraints = mock(FieldConstraints.class);
        when(mockConstraintsBuilder.createConstraintsInstance()).thenReturn(constraints);
        ArgumentCaptor<FieldDefinition> argument = ArgumentCaptor.forClass(FieldDefinition.class);

        fieldDefinitionBuilder.and();

        verify(mockParserBuilder).register(argument.capture());
        assertEquals(testFieldName, argument.getValue().getFieldName());
        verify(mockConstraintsBuilder).createConstraintsInstance();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParserBuilder() {
        new FieldDefinitionBuilder(null, testFieldName);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTestFieldName() {
        new FieldDefinitionBuilder(mockParserBuilder, null);
    }
}
