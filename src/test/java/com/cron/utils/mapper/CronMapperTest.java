package com.cron.utils.mapper;

import com.cron.utils.CronFieldName;
import com.cron.utils.model.CronDefinition;
import com.cron.utils.parser.field.Always;
import com.cron.utils.parser.field.CronField;
import com.cron.utils.parser.field.FieldExpression;
import com.cron.utils.parser.field.On;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronMapperTest {
    private CronFieldName testCronFieldName;
    @Mock
    private CronField mockCronField;
    private CronMapper mapper;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        this.testCronFieldName = CronFieldName.SECOND;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorSourceDefinitionNull() throws Exception {
        new CronMapper(mock(CronDefinition.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorTargetDefinitionNull() throws Exception {
        new CronMapper(null, mock(CronDefinition.class));
    }

    @Test
    public void testReturnSameExpression() throws Exception {
        Function<CronField, CronField> function = CronMapper.returnSameExpression();
        assertEquals(mockCronField, function.apply(mockCronField));
    }

    @Test
    public void testReturnOnZeroExpression() throws Exception {
        Function<CronField, CronField> function = CronMapper.returnOnZeroExpression(testCronFieldName);

        assertEquals(testCronFieldName, function.apply(mockCronField).getField());
        On result = (On)function.apply(mockCronField).getExpression();
        assertEquals(0, result.getTime());
    }

    @Test
    public void testReturnAlwaysExpression() throws Exception {
        Function<CronField, CronField> function = CronMapper.returnAlwaysExpression(testCronFieldName);

        assertEquals(testCronFieldName, function.apply(mockCronField).getField());
        assertEquals(Always.class, function.apply(mockCronField).getExpression().getClass());
    }
}