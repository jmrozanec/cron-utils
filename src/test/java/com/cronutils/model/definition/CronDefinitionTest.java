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

package com.cronutils.model.definition;

import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class CronDefinitionTest {

    private boolean matchDayOfWeekAndDayOfMonth;
    @Mock
    private FieldDefinition mockFieldDefinition1;
    @Mock
    private FieldDefinition mockFieldDefinition2;
    @Mock
    private FieldDefinition mockFieldDefinition3optional;

    @Before
    public void setUp() {
        final CronFieldName testFieldName1 = CronFieldName.SECOND;
        final CronFieldName testFieldName2 = CronFieldName.MINUTE;
        final CronFieldName testFieldName3 = CronFieldName.HOUR;
        MockitoAnnotations.initMocks(this);
        when(mockFieldDefinition1.getFieldName()).thenReturn(testFieldName1);
        when(mockFieldDefinition2.getFieldName()).thenReturn(testFieldName2);
        when(mockFieldDefinition3optional.getFieldName()).thenReturn(testFieldName3);
        when(mockFieldDefinition3optional.isOptional()).thenReturn(Boolean.TRUE);

        matchDayOfWeekAndDayOfMonth = false;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() {
        new CronDefinition(null, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraintsParameter() {
        new CronDefinition(new ArrayList<>(), null, new HashSet<>(), matchDayOfWeekAndDayOfMonth);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCronNicknamesParameter() {
        new CronDefinition(new ArrayList<>(), new HashSet<>(), null, matchDayOfWeekAndDayOfMonth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyFieldsParameter() {
        new CronDefinition(new ArrayList<>(), new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth);
    }

    @Test
    public void testLastFieldOptionalTrueWhenSet() {
        final List<FieldDefinition> fields = provideFieldDefinitionListWithOptionalDefinition();
        final Set<FieldDefinition> fieldDefinitions = new CronDefinition(fields, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth)
                .getFieldDefinitions();
        final List<FieldDefinition> sortedFieldDefinitions = new ArrayList<>(fieldDefinitions);
        sortedFieldDefinitions.sort(FieldDefinition.createFieldDefinitionComparator());
        assertTrue(sortedFieldDefinitions.get(fields.size() - 1).isOptional());
    }

    private List<FieldDefinition> provideFieldDefinitionListWithOptionalDefinition() {
        List<FieldDefinition> definitions = provideFieldDefinitionListWithSingleDefinition();
        definitions.add(mockFieldDefinition2);
        definitions.add(mockFieldDefinition3optional);
        return definitions;
    }

    private List<FieldDefinition> provideFieldDefinitionListWithSingleDefinition() {
        List<FieldDefinition> definitions = new ArrayList<>();
        definitions.add(mockFieldDefinition1);
        return definitions;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastFieldOptionalNotAllowedOnSingleFieldDefinition() {
        final List<FieldDefinition> fields = new ArrayList<>();
        fields.add(mockFieldDefinition3optional);
        new CronDefinition(fields, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth);
    }

    @Test
    public void testGetFieldDefinitions() {
        final List<FieldDefinition> fields = provideFieldDefinitionListWithSingleDefinition();
        final CronDefinition cronDefinition = new CronDefinition(fields, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth);
        assertNotNull(cronDefinition.getFieldDefinitions());
        assertEquals(1, cronDefinition.getFieldDefinitions().size());
        assertTrue(cronDefinition.getFieldDefinitions().contains(mockFieldDefinition1));
    }

    @Test
    public void simpleEqualityTest() {
        assertEquals(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    @Parameters(method = "parametersToTestIfEqual")
    public void testIfDefinitionsAreEqual(
        List<FieldDefinition> fieldDefinitionsOne,
        List<FieldDefinition> fieldDefinitionsTwo,
        Set<CronConstraint> cronConstraintsOne,
        Set<CronConstraint> cronConstraintsTwo,
        Set<CronNicknames> cronNicknamesOne,
        Set<CronNicknames> cronNicknamesTwo,
        boolean shouldMatchDayOfWeekAndDayOfMonthOne,
        boolean shouldMatchDayOfWeekAndDayOfMonthTwo,
        boolean shouldBeEqual) {
        // given
        CronDefinition definitionOne = new CronDefinition(
            fieldDefinitionsOne,
            cronConstraintsOne,
            cronNicknamesOne,
            shouldMatchDayOfWeekAndDayOfMonthOne);
        CronDefinition definitionTwo = new CronDefinition(
            fieldDefinitionsTwo,
            cronConstraintsTwo,
            cronNicknamesTwo,
            shouldMatchDayOfWeekAndDayOfMonthTwo);

        // when
        boolean areEqual = definitionOne.equals(definitionTwo);

        // then
        if (shouldBeEqual) {
            assertTrue(areEqual);
        } else {
            assertFalse(areEqual);
        }
    }

    @SuppressWarnings("unused")
    private Object[] parametersToTestIfEqual() {
        return new Object[] {
            new Object[] {
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                true
            },
            new Object[] {
                provideFieldDefinitions(CronFieldName.SECOND, b -> b.withValidRange(0, 59)),
                provideFieldDefinitions(CronFieldName.SECOND, b -> b.withValidRange(1, 60)),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            },
            new Object[] {
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithOneConstraint(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            },
            new Object[] {
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithOneNickname(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            },
            new Object[] {
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                false,
                true,
                false
            },
            new Object[] {
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithOneConstraint(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithOneNickname(),
                false,
                true,
                false
            }
        };
    }

    private List<FieldDefinition> provideFieldDefinitions(CronFieldName cronFieldName, UnaryOperator<FieldConstraintsBuilder> configurator) {
        List<FieldDefinition> defs = new ArrayList<>();
        defs.add(new FieldDefinition(cronFieldName, configurator.apply(
                FieldConstraintsBuilder.instance().forField(cronFieldName)).createConstraintsInstance()));

        return defs;
    }

    private List<FieldDefinition> provideSimpleFieldDefinitions() {
        return provideFieldDefinitions(CronFieldName.SECOND, b -> b);
    }

    private Set<CronNicknames> provideCronNicknameSetWithTwoNicknames() {
        Set<CronNicknames> constraints = provideCronNicknameSetWithOneNickname();
        constraints.add(CronNicknames.DAILY);
        return constraints;
    }

    private Set<CronNicknames> provideCronNicknameSetWithOneNickname() {
        Set<CronNicknames> constraints = new HashSet<>();
        constraints.add(CronNicknames.ANNUALLY);
        return constraints;
    }

    private Set<CronConstraint> provideCronConstraintSetWithTwoConstraints() {
        Set<CronConstraint> constraints = provideCronConstraintSetWithOneConstraint();
        constraints.add(CronConstraintsFactory.ensureEitherDayOfYearOrMonth());
        return constraints;
    }

    private Set<CronConstraint> provideCronConstraintSetWithOneConstraint() {
        Set<CronConstraint> constraints = new HashSet<>();
        constraints.add(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth());
        return constraints;
    }
}
