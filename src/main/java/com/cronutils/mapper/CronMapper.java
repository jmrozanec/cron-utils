/*
 * Copyright 2014 jmrozanec
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

package com.cronutils.mapper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.cronutils.Function;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.SingleCron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.expression.visitor.ValueMappingFieldExpressionVisitor;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;

import static com.cronutils.model.field.expression.FieldExpression.always;
import static com.cronutils.model.field.expression.FieldExpression.questionMark;

public class CronMapper {
    private final Map<CronFieldName, Function<CronField, CronField>> mappings;
    private final Function<Cron, Cron> cronRules;
    private final CronDefinition to;

    /**
     * Constructor.
     *
     * @param from      - source CronDefinition;
     *                  if null a NullPointerException will be raised
     * @param to        - target CronDefinition;
     *                  if null a NullPointerException will be raised
     * @param cronRules - cron rules
     */
    public CronMapper(final CronDefinition from, final CronDefinition to, final Function<Cron, Cron> cronRules) {
        Preconditions.checkNotNull(from, "Source CronDefinition must not be null");
        this.to = Preconditions.checkNotNull(to, "Destination CronDefinition must not be null");
        this.cronRules = Preconditions.checkNotNull(cronRules, "CronRules must not be null");
        mappings = new EnumMap<>(CronFieldName.class);
        buildMappings(from, to);
    }

    /**
     * Maps given cron to target cron definition.
     *
     * @param cron - Instance to be mapped;
     *             if null a NullPointerException will be raised
     * @return new Cron instance, never null;
     */
    public Cron map(final Cron cron) {
        Preconditions.checkNotNull(cron, "Cron must not be null");
        final List<CronField> fields = new ArrayList<>();
        for (final CronFieldName name : CronFieldName.values()) {
            if (mappings.containsKey(name)) {
                fields.add(mappings.get(name).apply(cron.retrieve(name)));
            }
        }
        return cronRules.apply(new SingleCron(to, fields)).validate();
    }

    /**
     * Creates a CronMapper that maps a cron4j expression to a quartz expression.
     * @return a CronMapper for mapping from cron4j to quartz
     */
    public static CronMapper fromCron4jToQuartz() {
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                setQuestionMark()
        );
    }

    public static CronMapper fromQuartzToCron4j() {
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J),
                sameCron()
        );
    }

    public static CronMapper fromQuartzToUnix() {
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX),
                sameCron()
        );
    }

    public static CronMapper fromUnixToQuartz() {
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                setQuestionMark()
        );
    }

    public static CronMapper fromQuartzToSpring() {
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING),
                setQuestionMark()
        );
    }

    public static CronMapper sameCron(final CronDefinition cronDefinition) {
        return new CronMapper(cronDefinition, cronDefinition, sameCron());
    }

    private static Function<Cron, Cron> sameCron() {
        return cron -> cron;
    }

    private static Function<Cron, Cron> setQuestionMark() {
        return cron -> {
            final CronField dow = cron.retrieve(CronFieldName.DAY_OF_WEEK);
            final CronField dom = cron.retrieve(CronFieldName.DAY_OF_MONTH);
            if (dow == null && dom == null) {
                return cron;
            }
            if (dow.getExpression() instanceof QuestionMark || dom.getExpression() instanceof QuestionMark) {
                return cron;
            }
            final Map<CronFieldName, CronField> fields = new EnumMap<>(CronFieldName.class);
            fields.putAll(cron.retrieveFieldsAsMap());
            if (dow.getExpression() instanceof Always) {
                fields.put(CronFieldName.DAY_OF_WEEK,
                        new CronField(CronFieldName.DAY_OF_WEEK, questionMark(), fields.get(CronFieldName.DAY_OF_WEEK).getConstraints()));
            } else {
                if (dom.getExpression() instanceof Always) {
                    fields.put(CronFieldName.DAY_OF_MONTH,
                            new CronField(CronFieldName.DAY_OF_MONTH, questionMark(), fields.get(CronFieldName.DAY_OF_MONTH).getConstraints()));
                } else {
                    cron.validate();
                }
            }
            return new SingleCron(cron.getCronDefinition(), new ArrayList<>(fields.values()));
        };
    }

    /**
     * Builds functions that map the fields from source CronDefinition to target.
     *
     * @param from - source CronDefinition
     * @param to   - target CronDefinition
     */
    private void buildMappings(final CronDefinition from, final CronDefinition to) {
        final Map<CronFieldName, FieldDefinition> sourceFieldDefinitions = getFieldDefinitions(from);
        final Map<CronFieldName, FieldDefinition> destFieldDefinitions = getFieldDefinitions(to);
        boolean startedDestMapping = false;
        boolean startedSourceMapping = false;
        for (final CronFieldName name : CronFieldName.values()) {
            final FieldDefinition destinationFieldDefinition = destFieldDefinitions.get(name);
            final FieldDefinition sourceFieldDefinition = sourceFieldDefinitions.get(name);
            if (destinationFieldDefinition != null) {
                startedDestMapping = true;
            }
            if (sourceFieldDefinition != null) {
                startedSourceMapping = true;
            }
            if (startedDestMapping && destinationFieldDefinition == null) {
                break;
            }
            //destination has fields before source definition starts. We default them to zero.
            if (!startedSourceMapping && destinationFieldDefinition != null) {
                mappings.put(name, returnOnZeroExpression(name));
            }
            //destination has fields after source definition was processed. We default them to always.
            if (startedSourceMapping && sourceFieldDefinition == null && destinationFieldDefinition != null) {
                mappings.put(name, returnAlwaysExpression(name));
            }
            if (sourceFieldDefinition == null || destinationFieldDefinition == null) {
                continue;
            }
            if (CronFieldName.DAY_OF_WEEK.equals(name)) {
                mappings.put(name, dayOfWeekMapping((DayOfWeekFieldDefinition) sourceFieldDefinition, (DayOfWeekFieldDefinition) destinationFieldDefinition));
            } else if (CronFieldName.DAY_OF_MONTH.equals(name)) {
                mappings.put(name, dayOfMonthMapping(sourceFieldDefinition, destinationFieldDefinition));
            } else {
                mappings.put(name, returnSameExpression());
            }
        }
    }

    private Map<CronFieldName, FieldDefinition> getFieldDefinitions(final CronDefinition from) {
        final Map<CronFieldName, FieldDefinition> result = new EnumMap<>(CronFieldName.class);

        for (final FieldDefinition fieldDefinition : from.getFieldDefinitions()) {
            result.put(fieldDefinition.getFieldName(), fieldDefinition);
        }
        return result;
    }

    /**
     * Creates a Function that returns same field.
     *
     * @return CronField -> CronField instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnSameExpression() {
        return field -> field;
    }

    /**
     * Creates a Function that returns a On instance with zero value.
     *
     * @param name - Cron field name
     * @return new CronField -> CronField instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnOnZeroExpression(final CronFieldName name) {
        return field -> {
            final FieldConstraints constraints = FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance();
            return new CronField(name, new On(new IntegerFieldValue(0)), constraints);
        };
    }

    /**
     * Creates a Function that returns an Always instance.
     *
     * @param name - Cron field name
     * @return new CronField -> CronField instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnAlwaysExpression(final CronFieldName name) {
        return field -> new CronField(name, always(), FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance());
    }

    @VisibleForTesting
    static Function<CronField, CronField> dayOfWeekMapping(final DayOfWeekFieldDefinition sourceDef, final DayOfWeekFieldDefinition targetDef) {
        return field -> {
            final FieldExpression expression = field.getExpression();
            FieldExpression dest = null;
            dest = expression.accept(
                    new ValueMappingFieldExpressionVisitor(
                            fieldValue -> {
                                if (fieldValue instanceof IntegerFieldValue) {
                                    return new IntegerFieldValue(
                                            ConstantsMapper.weekDayMapping(
                                                    sourceDef.getMondayDoWValue(),
                                                    targetDef.getMondayDoWValue(),
                                                    ((IntegerFieldValue) fieldValue).getValue()
                                            )
                                    );
                                }
                                return fieldValue;
                            }
                    )
            );

            if (expression instanceof QuestionMark && !targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)) {
                dest = always();
            }
            return new CronField(CronFieldName.DAY_OF_WEEK, dest, targetDef.getConstraints());

        };
    }

    @VisibleForTesting
    static Function<CronField, CronField> dayOfMonthMapping(final FieldDefinition sourceDef, final FieldDefinition targetDef) {
        return field -> {
            final FieldExpression expression = field.getExpression();
            FieldExpression dest = expression;
            if (expression instanceof QuestionMark && !targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)) {
                dest = always();
            }
            return new CronField(CronFieldName.DAY_OF_MONTH, dest, targetDef.getConstraints());
        };
    }
}
