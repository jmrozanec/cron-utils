package com.cronutils.mapper;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.On;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CronMapperTest {
    private CronFieldName testCronFieldName;
    @Mock
    private CronField mockCronField;
    private Function<Cron, Cron> cronMapping=null;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        this.testCronFieldName = CronFieldName.SECOND;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorSourceDefinitionNull() throws Exception {
        new CronMapper(mock(CronDefinition.class), null, cronMapping);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorTargetDefinitionNull() throws Exception {
        new CronMapper(null, mock(CronDefinition.class), cronMapping);
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
        assertEquals(0, (int)result.getTime().getValue());
    }

    @Test
    public void testReturnAlwaysExpression() throws Exception {
        Function<CronField, CronField> function = CronMapper.returnAlwaysExpression(testCronFieldName);

        assertEquals(testCronFieldName, function.apply(mockCronField).getField());
        assertEquals(Always.class, function.apply(mockCronField).getExpression().getClass());
    }
}