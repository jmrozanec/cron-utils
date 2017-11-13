package com.cronutils.parser;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.definition.TestCronDefinitionsFactory;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

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
public class CronParserTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private CronDefinition definition;

    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyExpression() throws Exception {
        Set<FieldDefinition> set = Sets.newHashSet();
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);

        parser.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNoMatchingExpression() throws Exception {
        Set<FieldDefinition> set = Sets.newHashSet();
        set.add(new FieldDefinition(CronFieldName.SECOND, FieldConstraintsBuilder.instance().createConstraintsInstance()));
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);

        parser.parse("* *");
    }

    @Test
    public void testParseIncompleteEvery() throws Exception {
        Set<FieldDefinition> set = Sets.newHashSet();
        set.add(new FieldDefinition(CronFieldName.SECOND, FieldConstraintsBuilder.instance().createConstraintsInstance()));
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Missing steps for expression: */");

        assertNotNull(parser.parse("*/"));
    }

    /**
     * Corresponds to issue#11
     * https://github.com/jmrozanec/cron-utils/issues/11
     * Reported case:
     * when parsing: "* *[triple space here]* * ?"
     * we receive: NumberFormatException with message For input string: ""
     * Expected: ignore multiple spaces, and parse the expression.
     */
    @Test
    public void testMultipleSpacesDoNotHurtParsingExpression() throws Exception {
        FieldDefinition minute = new FieldDefinition(CronFieldName.MINUTE, FieldConstraintsBuilder.instance().createConstraintsInstance());
        FieldDefinition hour = new FieldDefinition(CronFieldName.HOUR, FieldConstraintsBuilder.instance().createConstraintsInstance());
        FieldDefinition dom = new FieldDefinition(CronFieldName.DAY_OF_MONTH, FieldConstraintsBuilder.instance().createConstraintsInstance());
        FieldDefinition month = new FieldDefinition(CronFieldName.MONTH, FieldConstraintsBuilder.instance().createConstraintsInstance());
        FieldDefinition dow = new FieldDefinition(CronFieldName.DAY_OF_WEEK, FieldConstraintsBuilder.instance().createConstraintsInstance());
        Set<FieldDefinition> set = Sets.newHashSet();
        set.add(minute);
        set.add(hour);
        set.add(dom);
        set.add(month);
        set.add(dow);
        when(definition.getFieldDefinitions()).thenReturn(set);
        when(definition.getFieldDefinition(CronFieldName.MINUTE)).thenReturn(minute);
        when(definition.getFieldDefinition(CronFieldName.HOUR)).thenReturn(hour);
        when(definition.getFieldDefinition(CronFieldName.DAY_OF_MONTH)).thenReturn(dom);
        when(definition.getFieldDefinition(CronFieldName.MONTH)).thenReturn(month);
        when(definition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).thenReturn(dow);
        parser = new CronParser(definition);

        parser.parse("* *   * * *");
    }

    /**
     * Corresponds to issue#148
     * https://github.com/jmrozanec/cron-utils/issues/148
     */
    @Test
    public void testParseEveryXyears() {
        CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        parser.parse("0/59 0/59 0/23 1/30 1/11 ? 2017/3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectionOfZeroPeriod() {
        CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        parser.parse("0/0 0 0 1 1 ? 2017/3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectionOfPeriodUpperLimitExceedance() {
        CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        parser.parse("0/60 0 0 1 1 ? 2017/3");
    }

    @Test
    public void testParseExtendedQuartzCron() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        parser.parse("0 0 0 ? * ? 2017 1/14");
    }

    /**
     * Corresponds to issue#185
     * https://github.com/jmrozanec/cron-utils/issues/185
     */
    @Test
    public void testNoRejectionTwoOptionalFields() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        parser.parse("0 0 0 ? * ? 2017 1/14");
    }

    @Test // issue #180
    public void testThatEveryMinuteIsPreserved() {
        CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        Cron expression = parser.parse("0 0/1 * 1/1 * ? *");
        assertEquals("0 0/1 * 1/1 * ? *", expression.asString());
    }

    @Test
    public void testParseExtendedQuartzCronWithAsterixDoY() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        parser.parse("0 0 0 ? * ? 2017 *"); //i.e. same as "0 0 0 * * ? 2017" or "0 0 0 ? * * 2017"
    }

    @Test
    public void testParseExtendedQuartzCronWithQuestionMarkDoY() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        parser.parse("0 0 0 1 * ? 2017 ?"); //i.e. same as "0 0 0 1 * ? 2017" with question mark being omitted
    }
}
