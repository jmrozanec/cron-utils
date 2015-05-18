package com.cronutils.parser;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;

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
public class CronParserQuartzIntegrationTest {
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    /**
     * Corresponds to issue#11
     * https://github.com/jmrozanec/cron-utils/issues/11
     * Reported case:
     * when parsing: "* * * * $ ?"
     * we receive: NumberFormatException
     * Expected: throw IllegalArgumentException notifying invalid char was used
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharsDetected() throws Exception {
        parser.parse("* * * * $ ?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharsDetectedWithSingleSpecialChar() throws Exception {
        parser.parse("* * * * $W ?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharsDetectedWithHashExpression1() throws Exception {
        parser.parse("* * * * $#3 ?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharsDetectedWithHashExpression2() throws Exception {
        parser.parse("* * * * 3#$ ?");
    }

    /**
     * Issue #15: we should support L in range (ex.: L-3)
     */
    @Test
    public void testLSupportedInDoMRange() throws Exception {
        parser.parse("* * * L-3 * ?");
    }

    /**
     * Issue #15: we should support L in range (ex.: L-3), but not other special chars
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLSupportedInRange() throws Exception {
        parser.parse("* * * W-3 * ?");
    }

    @Test
    public void testNLSupported() throws Exception {
        parser.parse("* * * 3L * ?");
    }
}
