package com.cronutils.mapper;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cronutils.Function;

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
public class CronMapper {
    private Map<CronFieldName, Function<CronField, CronField>> mappings;
    private Function<Cron, Cron> cronRules;
    private CronDefinition to;

    /**
     * Constructor
     * @param from - source CronDefinition;
     *             if null a NullPointerException will be raised
     * @param to - target CronDefinition;
     *             if null a NullPointerException will be raised
     */
    public CronMapper(CronDefinition from, CronDefinition to, Function<Cron, Cron> cronRules){
        Preconditions.checkNotNull(from, "Source CronDefinition must not be null");
        this.to = Preconditions.checkNotNull(to, "Destination CronDefinition must not be null");
        this.cronRules = Preconditions.checkNotNull(cronRules, "CronRules must not be null");
        mappings = new HashMap<>();
        buildMappings(from, to);
    }

    /**
     * Maps given cron to target cron definition
     * @param cron - Instance to be mapped;
     *             if null a NullPointerException will be raised
     * @return new Cron instance, never null;
     */
    public Cron map(Cron cron) {
        Preconditions.checkNotNull(cron, "Cron must not be null");
        List<CronField> fields = new ArrayList<>();
        for(CronFieldName name : CronFieldName.values()){
            if(mappings.containsKey(name)){
                fields.add(mappings.get(name).apply(cron.retrieve(name)));
            }
        }
        return cronRules.apply(new Cron(to, fields)).validate();
    }


    public static CronMapper fromCron4jToQuartz(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                setQuestionMark()
        );
    }

    public static CronMapper fromQuartzToCron4j(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J),
                sameCron()
        );
    }

    public static CronMapper fromQuartzToUnix(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX),
                sameCron()
        );
    }

    public static CronMapper fromUnixToQuartz(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                setQuestionMark()
        );
    }

    public static CronMapper sameCron(CronDefinition cronDefinition){
        return new CronMapper(cronDefinition, cronDefinition, sameCron());
    }




    private static Function<Cron, Cron> sameCron(){
        return cron -> cron;
    }

    private static Function<Cron, Cron> setQuestionMark(){
        return cron -> {
            CronField dow = cron.retrieve(CronFieldName.DAY_OF_WEEK);
            CronField dom = cron.retrieve(CronFieldName.DAY_OF_MONTH);
            if(dow!=null && dom != null){
                if(dow.getExpression() instanceof QuestionMark || dom.getExpression() instanceof QuestionMark){
                    return cron;
                } else {
                    Map<CronFieldName, CronField> fields = new HashMap<>();
                    fields.putAll(cron.retrieveFieldsAsMap());
                    if(dow.getExpression() instanceof Always){
                        fields.put(CronFieldName.DAY_OF_WEEK, new CronField(CronFieldName.DAY_OF_WEEK, new QuestionMark(), fields.get(CronFieldName.DAY_OF_WEEK).getConstraints()));
                    }else{
                        if(dom.getExpression() instanceof Always){
                            fields.put(CronFieldName.DAY_OF_MONTH, new CronField(CronFieldName.DAY_OF_MONTH, new QuestionMark(), fields.get(CronFieldName.DAY_OF_MONTH).getConstraints()));
                        }else{
                            cron.validate();
                        }
                    }
                    return new Cron(cron.getCronDefinition(), new ArrayList<>(fields.values()));
                }
            }
            return cron;
        };
    }





    /**
     * Builds functions that map the fields from source CronDefinition to target
     * @param from - source CronDefinition
     * @param to - target CronDefinition
     */
    private void buildMappings(CronDefinition from, CronDefinition to){
        Map<CronFieldName, FieldDefinition> sourceFieldDefinitions = new HashMap<>();
        Map<CronFieldName, FieldDefinition> destFieldDefinitions = new HashMap<>();
        for(FieldDefinition fieldDefinition : from.getFieldDefinitions()){
            sourceFieldDefinitions.put(fieldDefinition.getFieldName(), fieldDefinition);
        }
        for(FieldDefinition fieldDefinition : to.getFieldDefinitions()){
            destFieldDefinitions.put(fieldDefinition.getFieldName(), fieldDefinition);
        }
        boolean startedDestMapping = false;
        boolean startedSourceMapping = false;
        for(CronFieldName name : CronFieldName.values()){
            if(destFieldDefinitions.get(name)!=null){
                startedDestMapping = true;
            }
            if(sourceFieldDefinitions.get(name)!=null){
                startedSourceMapping = true;
            }
            if(startedDestMapping && destFieldDefinitions.get(name) == null){
                break;
            }
            //destination has fields before source definition starts. We default them to zero.
            if(!startedSourceMapping && sourceFieldDefinitions.get(name) == null && destFieldDefinitions.get(name) != null){
                mappings.put(name, returnOnZeroExpression(name));
            }
            //destination has fields after source definition was processed. We default them to always.
            if(startedSourceMapping && sourceFieldDefinitions.get(name) == null && destFieldDefinitions.get(name) != null){
                mappings.put(name, returnAlwaysExpression(name));
            }
            if(sourceFieldDefinitions.get(name) != null && destFieldDefinitions.get(name) != null){
                if(CronFieldName.DAY_OF_WEEK.equals(name)){
                    mappings.put(name,
                            dayOfWeekMapping(
                                    (DayOfWeekFieldDefinition)sourceFieldDefinitions.get(name),
                                    (DayOfWeekFieldDefinition)destFieldDefinitions.get(name)
                            )
                    );
                }else{
                    if(CronFieldName.DAY_OF_MONTH.equals(name)){
                        mappings.put(name, dayOfMonthMapping(sourceFieldDefinitions.get(name), destFieldDefinitions.get(name)));
                    } else {
                        mappings.put(name, returnSameExpression());
                    }
                }
            }
        }
    }

    /**
     * Creates a Function that returns same field
     * @return Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnSameExpression(){
        return field -> field;
    }

    /**
     * Creates a Function that returns a On instance with zero value
     * @param name - Cron field name
     * @return new Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnOnZeroExpression(final CronFieldName name){
        return field -> {
            FieldConstraints constraints = FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance();
            return new CronField(name, new On(new IntegerFieldValue(0)), constraints);
        };
    }

    /**
     * Creates a Function that returns an Always instance
     * @param name  - Cron field name
     * @return new Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnAlwaysExpression(final CronFieldName name){
        return field -> new CronField(name, new Always(), FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance());
    }


    @VisibleForTesting
    static Function<CronField, CronField> dayOfWeekMapping(final DayOfWeekFieldDefinition sourceDef, final DayOfWeekFieldDefinition targetDef){
        return field -> {
            FieldExpression expression = field.getExpression();
            FieldExpression dest = null;
            dest = expression.accept(
                    new ValueMappingFieldExpressionVisitor(
                            fieldValue -> {
                                if(fieldValue instanceof IntegerFieldValue){
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
            if(expression instanceof QuestionMark){
                if(!targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)){
                    dest = new Always();
                }
            }
            return new CronField(CronFieldName.DAY_OF_WEEK, dest, targetDef.getConstraints());
        };
    }

    @VisibleForTesting
    static Function<CronField, CronField> dayOfMonthMapping(final FieldDefinition sourceDef, final FieldDefinition targetDef){
        return field -> {
            FieldExpression expression = field.getExpression();
            FieldExpression dest = expression;
            if(expression instanceof QuestionMark){
                if(!targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)){
                    dest = new Always();
                }
            }
            return new CronField(CronFieldName.DAY_OF_MONTH, dest, targetDef.getConstraints());
        };
    }
}
