package com.cronutils.validator;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
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
public class CronValidatorTest {

    private CronDefinition testCronDefinition;
    private String cron4jExpression = "* * * * *";
    private String invalidExpression = "* * * * * * *";

    private CronValidator cronValidator;

    @Before
    public void setUp() throws Exception {
        testCronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);
        cronValidator = new CronValidator(testCronDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCronDefinition() throws Exception {
        new CronValidator(null);
    }

    @Test
    public void testIsValidWithValidExpression() throws Exception {
        assertTrue(cronValidator.isValid(cron4jExpression));
    }

    @Test
    public void testIsValidWithInvalidExpression() throws Exception {
        assertFalse(cronValidator.isValid(invalidExpression));
    }

    @Test
    public void testIsValidWithNullExpression() throws Exception {
        assertFalse(cronValidator.isValid(null));
    }

    @Test
    public void testValidateWithValidExpression() throws Exception {
        assertEquals(cron4jExpression, cronValidator.validate(cron4jExpression));
    }

    @Test(expected = RuntimeException.class)
    public void testValidateWithInvalidExpression() throws Exception {
        cronValidator.validate(invalidExpression);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateWithNullExpression() throws Exception {
        cronValidator.validate(null);
    }
}