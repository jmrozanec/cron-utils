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
package com.cronutils.model.definition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDayOfWeekDefinitionBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.definition.FieldDefinitionBuilder;
import com.cronutils.model.field.definition.FieldQuestionMarkDefinitionBuilder;
import com.cronutils.model.field.definition.FieldSpecialCharsDefinitionBuilder;

import java.util.stream.Collectors;

/**
 * Builder that allows to define and create CronDefinition instances
 */
public class CronDefinitionBuilder {
    private static final int LEAP_YEAR_DAY_COUNT = 366;
    private final Map<CronFieldName, FieldDefinition> fields = new HashMap<>();
    private final Set<CronConstraint> cronConstraints = new HashSet<>();
    private boolean enforceStrictRanges;

    /**
     * Constructor.
     */
    private CronDefinitionBuilder() {/*NOP*/}

    /**
     * Creates a builder instance
     * @return new CronDefinitionBuilder instance
     */
    public static CronDefinitionBuilder defineCron() {
        return new CronDefinitionBuilder();
    }

    /**
     * Adds definition for seconds field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withSeconds() {
        return new FieldDefinitionBuilder(this, CronFieldName.SECOND);
    }

    /**
     * Adds definition for minutes field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMinutes() {
        return new FieldDefinitionBuilder(this, CronFieldName.MINUTE);
    }

    /**
     * Adds definition for hours field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withHours() {
        return new FieldDefinitionBuilder(this, CronFieldName.HOUR);
    }

    /**
     * Adds definition for day of month field
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder withDayOfMonth() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_MONTH);
    }

    /**
     * Adds definition for month field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMonth() {
        return new FieldDefinitionBuilder(this, CronFieldName.MONTH);
    }

    /**
     * Adds definition for day of week field
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withDayOfWeek() {
        return new FieldDayOfWeekDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
    }

    /**
     * Adds definition for year field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withYear() {
        return new FieldDefinitionBuilder(this, CronFieldName.YEAR);
    }
    
    /**
     * Adds definition for day of year field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldQuestionMarkDefinitionBuilder withDayOfYear() {
        return new FieldQuestionMarkDefinitionBuilder(this, CronFieldName.DAY_OF_YEAR);
    }

    /**
     * Sets enforceStrictRanges value to true
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder enforceStrictRanges() {
        enforceStrictRanges = true;
        return this;
    }

    /**
     * Adds a cron validation
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withCronValidation(CronConstraint validation) {
        this.cronConstraints.add(validation);
        return this;
    }

    /**
     * Registers a certain FieldDefinition
     * @param definition - FieldDefinition  instance, never null
     */
    public void register(FieldDefinition definition) {
        //ensure that we can't register a mandatory definition if there are already optional ones
        if (!definition.isOptional() && fields.values().stream().anyMatch(def -> def.isOptional()))
            throw new IllegalArgumentException("Can't register mandatory definition after a optional definition.");
        fields.put(definition.getFieldName(), definition);
    }

    /**
     * Creates a new CronDefinition instance with provided field definitions
     * @return returns CronDefinition instance, never null
     */
    public CronDefinition instance() {
        Set<CronConstraint> validations = new HashSet<CronConstraint>();
        validations.addAll(cronConstraints);
        return new CronDefinition(fields.values().stream().sorted((o1,o2) -> o1.getFieldName().getOrder() - o2.getFieldName().getOrder()).collect(Collectors.toList()), validations, enforceStrictRanges);
    }

    /**
     * Creates CronDefinition instance matching cron4j specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition cron4j() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0,6).withMondayDoWValue(1).and()
                .enforceStrictRanges()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching quartz specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition quartz() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }

    /**
     * Creates CronDefinition instance matching unix crontab specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition unixCrontab() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0,7).withMondayDoWValue(1).withIntMapping(7,0).and()
                .enforceStrictRanges()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching cronType specification;
     * @param cronType - some cron type. If null, a RuntimeException will be raised.
     * @return CronDefinition instance if definition is found; a RuntimeException otherwise.
     */
    public static CronDefinition instanceDefinitionFor(CronType cronType){
        switch (cronType){
            case CRON4J:
                return cron4j();
            case QUARTZ:
                return quartz();
            case UNIX:
                return unixCrontab();
            default:
                throw new RuntimeException(String.format("No cron definition found for %s", cronType));
        }
    }
}

