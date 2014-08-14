package com.cron.utils.parser;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.CronField;

import java.util.HashMap;
import java.util.HashSet;
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
public class ParserDefinitionBuilder {
    private Map<CronFieldName, CronField> fields;
    private boolean lastFieldOptional;

    private ParserDefinitionBuilder() {
        fields = new HashMap<CronFieldName, CronField>();
        lastFieldOptional = false;
    }

    public static ParserDefinitionBuilder defineParser() {
        return new ParserDefinitionBuilder();
    }

    public FieldDefinitionBuilder withSeconds() {
        return new FieldDefinitionBuilder(this, CronFieldName.SECOND);
    }

    public FieldDefinitionBuilder withMinutes() {
        return new FieldDefinitionBuilder(this, CronFieldName.MINUTE);
    }

    public FieldDefinitionBuilder withHours() {
        return new FieldDefinitionBuilder(this, CronFieldName.HOUR);
    }

    public FieldSpecialCharsDefinitionBuilder withDayOfMonth() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_MONTH);
    }

    public FieldDefinitionBuilder withMonth() {
        return new FieldDefinitionBuilder(this, CronFieldName.MONTH);
    }

    public FieldSpecialCharsDefinitionBuilder withDayOfWeek() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
    }

    public FieldDefinitionBuilder withYear() {
        return new FieldDefinitionBuilder(this, CronFieldName.YEAR);
    }

    public ParserDefinitionBuilder lastFieldOptional() {
        lastFieldOptional = true;
        return this;
    }

    void register(CronField cronField) {
        fields.put(cronField.getField(), cronField);
    }

    public CronParser instance() {
        return new CronParser(new HashSet<CronField>(fields.values()), lastFieldOptional);
    }
}
