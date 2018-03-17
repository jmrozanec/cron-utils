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

package com.cronutils.model;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.utils.Preconditions;

/**
 * Represents a cron expression.
 */
public interface Cron extends Serializable {

    /**
     * Retrieve value for cron field.
     *
     * @param name - cron field name.
     *             If null, a NullPointerException will be raised.
     * @return CronField that corresponds to given CronFieldName
     */
    CronField retrieve(final CronFieldName name);

    /**
     * Retrieve all cron field values as map.
     *
     * @return unmodifiable Map with key CronFieldName and values CronField, never null
     */
    Map<CronFieldName, CronField> retrieveFieldsAsMap();

    String asString();

    CronDefinition getCronDefinition();

    /**
     * Validates this Cron instance by validating its cron expression.
     *
     * @return this Cron instance
     * @throws IllegalArgumentException if the cron expression is invalid
     */
    Cron validate();

    /**
     * Provides means to compare if two cron expressions are equivalent.
     *
     * @param cronMapper - maps 'cron' parameter to this instance definition;
     * @param cron       - any cron instance, never null
     * @return boolean - true if equivalent; false otherwise.
     */
    boolean equivalent(final CronMapper cronMapper, final Cron cron);

    /**
     * Provides means to compare if two cron expressions are equivalent.
     * Assumes same cron definition.
     *
     * @param cron - any cron instance, never null
     * @return boolean - true if equivalent; false otherwise.
     */
    boolean equivalent(final Cron cron);
}

