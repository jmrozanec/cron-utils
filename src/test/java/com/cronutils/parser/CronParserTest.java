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

package com.cronutils.parser;

import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.definition.TestCronDefinitionsFactory;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CronParserTest {
    @Mock
    private CronDefinition definition;

    private CronParser parser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseEmptyExpression() {
        when(definition.getFieldDefinitions()).thenReturn(Collections.emptySet());
        parser = new CronParser(definition);

        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
    }

    @Test
    public void testParseNoMatchingExpression() {
        final Set<FieldDefinition> set =
                Collections.singleton(new FieldDefinition(CronFieldName.SECOND, FieldConstraintsBuilder.instance().createConstraintsInstance()));
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);

        assertThrows(IllegalArgumentException.class, () -> parser.parse("* *"));
    }

    @Test
    public void testParseIncompleteEvery() {
        parseIncompleteExpression("*/","Missing steps for expression: */");
    }

    private static void validateExpression(CronType cronType, String expression) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
        CronParser parser = new CronParser(cronDefinition);
        System.out.println(String.format("Validating expression '%s' using %s definition", expression, cronType));
        parser.parse(expression);
    }

    @Test // issue #368
    public void testTrailingCommaListCron4j(){
        assertThrows(IllegalArgumentException.class, () -> validateExpression(CronType.CRON4J, "1, * * * *"));
    }

    @Test // issue #368
    public void testTrailingCommaListQuartz(){
        assertThrows(IllegalArgumentException.class, () -> validateExpression(CronType.QUARTZ, "1, * * * * ?"));
    }

    @Test // issue #368
    public void testTrailingCommaListSpring(){
        assertThrows(IllegalArgumentException.class, () -> validateExpression(CronType.SPRING, "1,2, * * * * ?"));
    }

    @Test // issue #368
    public void testTrailingCommaListUnix(){
        assertThrows(IllegalArgumentException.class, () -> validateExpression(CronType.UNIX, "1, * * * *"));
    }

    @Test
    public void testHashListUnix(){
        assertThrows(IllegalArgumentException.class, () -> validateExpression(CronType.UNIX, "0 0 0 ? * #"));
    }

    @Test // issue #369
    public void testParseIncompleteRangeNoValues() {
        parseIncompleteExpression("-", "Missing values for range: -");
    }

    @Test // issue #369
    public void testParseIncompleteRangeOnlyLeftValue() {
        parseIncompleteExpression("1-", "Missing values for range: 1-");
    }

    private void parseIncompleteExpression(String expression, String expectedMessage) {
        final Set<FieldDefinition> set =
                Collections.singleton(new FieldDefinition(CronFieldName.SECOND, FieldConstraintsBuilder.instance().createConstraintsInstance()));
        when(definition.getFieldDefinitions()).thenReturn(set);
        parser = new CronParser(definition);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parse(expression));
        assertTrue(e.getMessage().contains(expectedMessage));
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
    public void testMultipleSpacesDoNotHurtParsingExpression() {
        final FieldDefinition minute = new FieldDefinition(CronFieldName.MINUTE, FieldConstraintsBuilder.instance().createConstraintsInstance());
        final FieldDefinition hour = new FieldDefinition(CronFieldName.HOUR, FieldConstraintsBuilder.instance().createConstraintsInstance());
        final FieldDefinition dom = new FieldDefinition(CronFieldName.DAY_OF_MONTH, FieldConstraintsBuilder.instance().createConstraintsInstance());
        final FieldDefinition month = new FieldDefinition(CronFieldName.MONTH, FieldConstraintsBuilder.instance().createConstraintsInstance());
        final FieldDefinition dow = new FieldDefinition(CronFieldName.DAY_OF_WEEK, FieldConstraintsBuilder.instance().createConstraintsInstance());
        final Set<FieldDefinition> set = new HashSet<>();
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
        final CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        parser.parse("0/59 0/59 0/23 1/30 1/11 ? 2017/3");
    }

    @Test
    public void testRejectionOfZeroPeriod() {
        final CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        assertThrows(IllegalArgumentException.class, () -> parser.parse("0/0 0 0 1 1 ? 2017/3"));
    }

    @Test
    public void testRejectionOfPeriodUpperLimitExceedance() {
        final CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);
        assertThrows(IllegalArgumentException.class, () -> parser.parse("0/60 0 0 1 1 ? 2017/3"));
    }

    @Test
    public void testParseExtendedQuartzCron() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        parser.parse("0 0 0 ? * ? 2017 1/14");
    }

    @Test // issue #180
    public void testThatEveryMinuteIsPreserved() {
        final CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        parser = new CronParser(quartzDefinition);

        final Cron expression = parser.parse("0 0/1 * 1/1 * ? *");
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

    @Test
    public void testParseMulticron(){
        String multicron = "0 0|0|30|0 9|10|11|12 * * ? *";
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        Cron cron = parser.parse(multicron);
        assertEquals(multicron, cron.asString());
    }

    @Test
    public void testParseQuartzCronWithHash() {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        assertThrows(IllegalArgumentException.class, () -> parser.parse("0 0 0 ? * #"));
    }
    
    /**
     * Test parse() method for composite cron expression (i.e. expression containing '||')
     * @param expression Input parameters for parse() method 
     */
    @ParameterizedTest
    @ValueSource(strings = {"2 0 0 1 * ? 2000 ? || 5 0 0 1 * ? 2017 ?", "5 3 0 ? * ? 1998 * || 1 0 0 ? * * 2023 ? || 3 3 * * * ? 2015 ?"})
    public void testParseWithCompositeCron(String expression) {
    	parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
    	CompositeCron compositeCron = (CompositeCron) parser.parse(expression);
    	String[] expectedCrons = expression.split("\\|\\|");
    	List<Cron> crons = compositeCron.getCrons();
    	assertEquals(expectedCrons.length, crons.size());
    	for(int i = 0; i < crons.size(); i++) {
    		assertEquals(expectedCrons[i].trim(), crons.get(i).asString());
    	}
    }
}
