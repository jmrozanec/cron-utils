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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CronDefinitionTest {

    private boolean matchDayOfWeekAndDayOfMonth;
    @Mock
    private FieldDefinition mockFieldDefinition1;
    @Mock
    private FieldDefinition mockFieldDefinition2;
    @Mock
    private FieldDefinition mockFieldDefinition3optional;

    @BeforeEach
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

    @Test
    public void testConstructorNullFieldsParameter() {
        assertThrows(NullPointerException.class, () -> new CronDefinition(null, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth));
    }

    @Test
    public void testConstructorNullConstraintsParameter() {
        assertThrows(NullPointerException.class, () -> new CronDefinition(new ArrayList<>(), null, new HashSet<>(), matchDayOfWeekAndDayOfMonth));
    }

    @Test
    public void testConstructorNullCronNicknamesParameter() {
        assertThrows(NullPointerException.class, () -> new CronDefinition(new ArrayList<>(), new HashSet<>(), null, matchDayOfWeekAndDayOfMonth));
    }

    @Test
    public void testConstructorEmptyFieldsParameter() {
        assertThrows(IllegalArgumentException.class, () -> new CronDefinition(new ArrayList<>(), new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth));
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

    @Test
    public void testLastFieldOptionalNotAllowedOnSingleFieldDefinition() {
        final List<FieldDefinition> fields = new ArrayList<>();
        fields.add(mockFieldDefinition3optional);
        assertThrows(IllegalArgumentException.class, () -> new CronDefinition(fields, new HashSet<>(), new HashSet<>(), matchDayOfWeekAndDayOfMonth));
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

    @ParameterizedTest
    @MethodSource("parametersToTestIfEqual")
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
    private static Stream<Arguments> parametersToTestIfEqual() {
        return Stream.of(
            Arguments.of(
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                true
            ),
            Arguments.of(
                provideFieldDefinitions(CronFieldName.SECOND, b -> b.withValidRange(0, 59)),
                provideFieldDefinitions(CronFieldName.SECOND, b -> b.withValidRange(1, 60)),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            ),
            Arguments.of(
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithOneConstraint(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            ),
            Arguments.of(
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithOneNickname(),
                provideCronNicknameSetWithTwoNicknames(),
                true,
                true,
                false
            ),
            Arguments.of(
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithTwoNicknames(),
                false,
                true,
                false
            ),
            Arguments.of(
                provideSimpleFieldDefinitions(),
                provideSimpleFieldDefinitions(),
                provideCronConstraintSetWithTwoConstraints(),
                provideCronConstraintSetWithOneConstraint(),
                provideCronNicknameSetWithTwoNicknames(),
                provideCronNicknameSetWithOneNickname(),
                false,
                true,
                false
            )
        );
    }

    private static List<FieldDefinition> provideFieldDefinitions(CronFieldName cronFieldName, UnaryOperator<FieldConstraintsBuilder> configurator) {
        List<FieldDefinition> defs = new ArrayList<>();
        defs.add(new FieldDefinition(cronFieldName, configurator.apply(
                FieldConstraintsBuilder.instance().forField(cronFieldName)).createConstraintsInstance()));

        return defs;
    }

    private static List<FieldDefinition> provideSimpleFieldDefinitions() {
        return provideFieldDefinitions(CronFieldName.SECOND, b -> b);
    }

    private static Set<CronNicknames> provideCronNicknameSetWithTwoNicknames() {
        Set<CronNicknames> constraints = provideCronNicknameSetWithOneNickname();
        constraints.add(CronNicknames.DAILY);
        return constraints;
    }

    private static Set<CronNicknames> provideCronNicknameSetWithOneNickname() {
        Set<CronNicknames> constraints = new HashSet<>();
        constraints.add(CronNicknames.ANNUALLY);
        return constraints;
    }

    private static Set<CronConstraint> provideCronConstraintSetWithTwoConstraints() {
        Set<CronConstraint> constraints = provideCronConstraintSetWithOneConstraint();
        constraints.add(CronConstraintsFactory.ensureEitherDayOfYearOrMonth());
        return constraints;
    }

    private static Set<CronConstraint> provideCronConstraintSetWithOneConstraint() {
        Set<CronConstraint> constraints = new HashSet<>();
        constraints.add(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth());
        return constraints;
    }
}
