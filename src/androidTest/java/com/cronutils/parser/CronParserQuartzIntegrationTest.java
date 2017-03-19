package com.cronutils.parser;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpressionFactory;
import com.cronutils.model.time.ExecutionTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.junit.runner.RunWith;
import org.threeten.bp.ZonedDateTime;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
@RunWith(AndroidJUnit4.class)
public class CronParserQuartzIntegrationTest extends BaseAndroidTest {
    private CronParser parser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        super.setUp();
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

    /**
     * Issue #23: we should support L in DoM
     */
    @Test
    public void testLSupportedInDoM() throws Exception {
        parser.parse("0 0/10 22 L * ?");
    }

    /**
     * Issue #27: month ranges string mapping
     */
    @Test
    public void testMonthRangeStringMapping() {
        parser.parse("0 0 0 * JUL-AUG ? *");
        parser.parse("0 0 0 * JAN-FEB ? *");
    }

    /**
     * Issue #27: month string mapping
     */
    @Test
    public void testSingleMonthStringMapping() {
        parser.parse("0 0 0 * JAN ? *");
    }

    /**
     * Issue #27: day of week string ranges mapping
     */
    @Test
    public void testDoWRangeStringMapping() {
        parser.parse("0 0 0 ? * MON-FRI *");
    }

    /**
     * Issue #27: day of week string mapping
     */
    @Test
    public void testSingleDoWStringMapping() {
        parser.parse("0 0 0 ? * MON *");
    }

    /**
     * Issue #27: July month as string is parsed as some special char occurrence
     */
    @Test
    public void testJulyMonthAsStringConsideredSpecialChar() {
        assertNotNull(parser.parse("0 0 0 * JUL ? *"));
    }

    /**
     * Issue #35: A>B in range considered invalid expression for Quartz.
     */
    @Test
    public void testSunToSat() {
        // FAILS SUN-SAT: SUN = 7 and SAT = 6
        parser.parse("0 0 12 ? * SUN-SAT");
    }

    /**
     * Issue #39: reported issue about exception being raised on parse.
     */
    @Test
    public void testParseExpressionWithQuestionMarkAndWeekdays() {
        parser.parse("0 0 0 ? * MON,TUE *");
    }

    /**
     * Issue #39: reported issue about exception being raised on parse.
     */
    @Test
    public void testDescribeExpressionWithQuestionMarkAndWeekdays() {
        Cron quartzCron = parser.parse("0 0 0 ? * MON,TUE *");
        CronDescriptor descriptor = CronDescriptor.instance(Locale.ENGLISH);
        descriptor.describe(quartzCron);
    }

    /**
     * Issue #60: Parser exception when parsing cron:
     */
    @Test
    public void testDescribeExpression() {
        String expression = "0 * * ? * 1,5";
        CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(definition);
        Cron c = parser.parse(expression);
        CronDescriptor.instance(Locale.GERMAN).describe(c);
    }

    /**
     * Issue #63: Parser exception when parsing cron:
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDoMAndDoWParametersInvalidForQuartz() {
        parser.parse("0 30 17 4 1 * 2016");
    }

    /**
     * Issue #78: ExecutionTime.forCron fails on intervals
     */
    @Test
    public void testIntervalSeconds() {
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0/20 * * * * ?"));
        ZonedDateTime now = ZonedDateTime.parse("2005-08-09T18:32:42Z");
        ZonedDateTime lastExecution = executionTime.lastExecution(now).get();
        ZonedDateTime assertDate = ZonedDateTime.parse("2005-08-09T18:32:40Z");
        assertEquals(assertDate, lastExecution);
    }

    /**
     * Issue #78: ExecutionTime.forCron fails on intervals
     */
    @Test
    public void testIntervalMinutes() {
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0/7 * * * ?"));
        ZonedDateTime now = ZonedDateTime.parse("2005-08-09T18:32:42Z");
        ZonedDateTime lastExecution = executionTime.lastExecution(now).get();
        ZonedDateTime assertDate = ZonedDateTime.parse("2005-08-09T18:28:00Z");
        assertEquals(assertDate, lastExecution);
    }

    /**
     * Issue #89: regression - NumberFormatException: For input string: "$"
     */
    @Test
    public void testRegressionDifferentMessageForException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid chars in expression! Expression: $ Invalid chars: $");
        assertNotNull(ExecutionTime.forCron(parser.parse("* * * * $ ?")));
    }

    /**
     * Issue #90: Reported error contains other expression than the one provided
     */
    @Test
    public void testReportedErrorContainsSameExpressionAsProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                "Invalid cron expression: 0 * * * * *. Both, a day-of-week AND a day-of-month parameter, are not supported.");
        assertNotNull(ExecutionTime.forCron(parser.parse("0/1 * * * * *")));
    }

    /**
     * Issue #109: Missing expression and invalid chars in error message
     * https://github.com/jmrozanec/cron-utils/issues/109
     */
    @Test
    public void testMissingExpressionAndInvalidCharsInErrorMessage() {
        thrown.expect(IllegalArgumentException.class);
        String cronexpression = "* * -1 * * ?";
        thrown.expectMessage(String.format("Failed to parse '%s'. Invalid expression! Expression: -1 does not describe a range. Negative numbers are not allowed.", cronexpression));
        assertNotNull(ExecutionTime.forCron(parser.parse(cronexpression)));
    }

    /**
     * Issue #148: Cron Builder/Parser fails on Every X years
     */
//    @Test //TODO
    public void testEveryXYears(){
        CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)).withDoM(FieldExpressionFactory.on(1))
                .withDoW(FieldExpressionFactory.questionMark())
                .withYear(FieldExpressionFactory.every(FieldExpressionFactory.between(1970, 2099), 4))
                .withMonth(FieldExpressionFactory.on(0))
                .withHour(FieldExpressionFactory.on(0))
                .withMinute(FieldExpressionFactory.on(0))
                .withSecond(FieldExpressionFactory.on(0));
    }

    /**
     * Issue #151: L-7 in day of month should work to find the day 7 days prior to the last day of the month.
     */
//    @Test TODO
    public void testLSupportedInDoMRangeNextExecutionCalculation() {
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 15 10 L-7 * ?"));
        ZonedDateTime now = ZonedDateTime.parse("2017-01-31T10:00:00Z");
        ZonedDateTime nextExecution = executionTime.nextExecution(now).get();
        ZonedDateTime assertDate = ZonedDateTime.parse("2017-02-21T10:15:00Z");
        assertEquals(assertDate, nextExecution);
    }

    /**
     * Issue #154: Quartz Cron Year Pattern is not fully supported - i.e. increments on years are not supported
     * https://github.com/jmrozanec/cron-utils/issues/154
     * Duplicate of #148
     */
//    @Test TODO
    public void supportQuartzCronExpressionIncrementsOnYears() {
        final String[] sampleCronExpressions = {
                "0 0 0 1 * ? 2017/2",
                "0 0 0 1 * ? 2017/3",
                "0 0 0 1 * ? 2017/10",
                "0 0 0 1 * ? 2017-2047/2",
        };

        final CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        for (final String cronExpression: sampleCronExpressions) {
            final Cron quartzCron = quartzCronParser.parse(cronExpression);
            quartzCron.validate();
        }
    }

    @Test
    public void testErrorAbout2Parts() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cron expression contains 2 parts but we expect one of [6, 7]");
        assertNotNull(ExecutionTime.forCron(parser.parse("* *")));
    }

    @Test
    public void testErrorAboutMissingSteps() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Missing steps for expression: */");
        assertNotNull(ExecutionTime.forCron(parser.parse("*/ * * * * ?")));
    }
}
