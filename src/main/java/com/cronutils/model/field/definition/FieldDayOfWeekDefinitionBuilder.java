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

package com.cronutils.model.field.definition;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.utils.Preconditions;

public class FieldDayOfWeekDefinitionBuilder extends FieldSpecialCharsDefinitionBuilder {
    private int mondayDoWValue = 1;//default is cron-utils specification

    /**
     * Constructor.
     *
     * @param cronDefinitionBuilder - ParserDefinitionBuilder instance -
     *                              if null, a NullPointerException will be raised
     * @param fieldName             - CronFieldName instance -
     */
    public FieldDayOfWeekDefinitionBuilder(final CronDefinitionBuilder cronDefinitionBuilder, final CronFieldName fieldName) {
        super(cronDefinitionBuilder, fieldName);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(fieldName), "CronFieldName must be DAY_OF_WEEK");
    }

    /**
     * Registers the field supports the W (W) special char.
     *
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withMondayDoWValue(final int mondayDoW) {
        constraints.withShiftedStringMapping(mondayDoW - mondayDoWValue);
        mondayDoWValue = mondayDoW;
        return this;
    }

    /**
     * Registers CronField in ParserDefinitionBuilder and returns its instance.
     *
     * @return ParserDefinitionBuilder instance obtained from constructor
     */
    @Override
    public CronDefinitionBuilder and() {
        final boolean zeroInRange = constraints.createConstraintsInstance().isInRange(0);
        cronDefinitionBuilder
                .register(new DayOfWeekFieldDefinition(fieldName, constraints.createConstraintsInstance(), optional, new WeekDay(mondayDoWValue, zeroInRange)));
        return cronDefinitionBuilder;
    }

    /**
     * Allows to set a range of valid values for field.
     *
     * @param startRange - start range value
     * @param endRange   - end range value
     * @return same FieldDayOfWeekDefinitionBuilder instance
     */
    @Override
    public FieldDayOfWeekDefinitionBuilder withValidRange(final int startRange, final int endRange) {
        super.withValidRange(startRange, endRange);
        return this;
    }

    /**
     * Defines mapping between integer values with equivalent meaning.
     *
     * @param source - higher value
     * @param dest   - lower value with equivalent meaning to source
     * @return this FieldDayOfWeekDefinitionBuilder instance
     */
    @Override
    public FieldDayOfWeekDefinitionBuilder withIntMapping(final int source, final int dest) {
        super.withIntMapping(source, dest);
        return this;
    }
}
