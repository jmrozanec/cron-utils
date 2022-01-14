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

package com.cronutils.model.field.definition;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;

/**
 * Builder that allows to specify properties for a cron field supporting question mark,.
 * i.e. "no specific value" - useful when you need to specify something in one of
 * the two ore more fields in which the character is allowed.
 */
public class FieldQuestionMarkDefinitionBuilder extends FieldDefinitionBuilder {

    /**
     * Constructor.
     *
     * @param cronDefinitionBuilder - ParserDefinitionBuilder instance -
     *                              if null, a NullPointerException will be raised
     * @param fieldName             - CronFieldName instance -
     *                              if null, a NullPointerException will be raised
     */
    public FieldQuestionMarkDefinitionBuilder(final CronDefinitionBuilder cronDefinitionBuilder, final CronFieldName fieldName) {
        super(cronDefinitionBuilder, fieldName);
    }

    /**
     * Registers the field supports the LW (LW) special char.
     *
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldQuestionMarkDefinitionBuilder supportsQuestionMark() {
        constraints.addQuestionMarkSupport();
        return this;
    }

}
