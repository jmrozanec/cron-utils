package com.cronutils.mapper;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.Always;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.On;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
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
    private CronDefinition to;

    /**
     * Constructor
     * @param from - source CronDefinition;
     *             if null a NullPointerException will be raised
     * @param to - target CronDefinition;
     *             if null a NullPointerException will be raised
     */
    public CronMapper(CronDefinition from, CronDefinition to){
        Validate.notNull(from, "Source CronDefinition must not be null");
        this.to = Validate.notNull(to, "Destination CronDefinition must not be null");
        mappings = Maps.newHashMap();
        buildMappings(from, to);
    }

    /**
     * Maps given cron to target cron definition
     * @param cron - Instance to be mapped;
     *             if null a NullPointerException will be raised
     * @return new Cron instance, never null;
     */
    public Cron map(Cron cron) {
        Validate.notNull(cron, "Cron must not be null");
        List<CronField> fields = Lists.newArrayList();
        for(CronFieldName name : CronFieldName.values()){
            if(mappings.containsKey(name)){
                fields.add(mappings.get(name).apply(cron.retrieve(name)));
            }
        }
        return new Cron(to, fields);
    }

    /**
     * Builds functions that map the fields from source CronDefinition to target
     * @param from - source CronDefinition
     * @param to - target CronDefinition
     */
    private void buildMappings(CronDefinition from, CronDefinition to){
        Map<CronFieldName, FieldDefinition> sourceFieldDefinitions = Maps.newHashMap();
        Map<CronFieldName, FieldDefinition> destFieldDefinitions = Maps.newHashMap();
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
                mappings.put(name, returnSameExpression());
            }
        }
    }

    /**
     * Creates a Function that returns same field
     * @return Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnSameExpression(){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return field;
            }
        };
    }

    /**
     * Creates a Function that returns a On instance with zero value
     * @param name - Cron field name
     * @return new Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnOnZeroExpression(final CronFieldName name){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return new CronField(name, new On(FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance(),"0"));
            }
        };
    }

    /**
     * Creates a Function that returns an Always instance
     * @param name  - Cron field name
     * @return new Function<CronField, CronField> instance, never null
     */
    @VisibleForTesting
    static Function<CronField, CronField> returnAlwaysExpression(final CronFieldName name){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return new CronField(name, new Always(FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance()));
            }
        };
    }
}
