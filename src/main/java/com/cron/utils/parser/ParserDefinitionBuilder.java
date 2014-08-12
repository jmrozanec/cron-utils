package com.cron.utils.parser;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.CronField;
import com.cron.utils.parser.field.FieldConstraintsBuilder;

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

    public FieldBuilderNotAllowSpecialChars withSeconds() {
        return new FieldBuilderNotAllowSpecialChars(this, CronFieldName.SECOND);
    }

    public FieldBuilderNotAllowSpecialChars withMinutes() {
        return new FieldBuilderNotAllowSpecialChars(this, CronFieldName.MINUTE);
    }

    public FieldBuilderNotAllowSpecialChars withHours() {
        return new FieldBuilderNotAllowSpecialChars(this, CronFieldName.HOUR);
    }

    public FieldBuilderAllowSpecialChars withDayOfMonth() {
        return new FieldBuilderAllowSpecialChars(this, CronFieldName.DAY_OF_MONTH);
    }

    public FieldBuilderNotAllowSpecialChars withMonth() {
        return new FieldBuilderNotAllowSpecialChars(this, CronFieldName.MONTH);
    }

    public FieldBuilderAllowSpecialChars withDayOfWeek() {
        return new FieldBuilderAllowSpecialChars(this, CronFieldName.DAY_OF_WEEK);
    }

    public FieldBuilderNotAllowSpecialChars withYear() {
        return new FieldBuilderNotAllowSpecialChars(this, CronFieldName.YEAR);
    }

    public ParserDefinitionBuilder lastFieldOptional() {
        lastFieldOptional = true;
        return this;
    }

    private void register(CronField cronField) {
        fields.put(cronField.getField(), cronField);
    }

    public CronParser instance() {
        return new CronParser(new HashSet<CronField>(fields.values()), lastFieldOptional);
    }

    class FieldBuilderNotAllowSpecialChars {
        private ParserDefinitionBuilder parserBuilder;
        private CronFieldName fieldName;
        private FieldConstraintsBuilder constraints;

        public FieldBuilderNotAllowSpecialChars(ParserDefinitionBuilder parserBuilder, CronFieldName fieldName){
            this.parserBuilder = parserBuilder;
            this.fieldName = fieldName;
            this.constraints = FieldConstraintsBuilder.instance().forField(fieldName);
        }

        public FieldBuilderNotAllowSpecialChars withIntMapping(int source, int dest){
            constraints.withIntValueMapping(source, dest);
            return this;
        }

        public ParserDefinitionBuilder and(){
            parserBuilder.register(new CronField(fieldName, constraints.createConstraintsInstance()));
            return parserBuilder;
        }
    }

    class FieldBuilderAllowSpecialChars {
        private ParserDefinitionBuilder parserBuilder;
        private CronFieldName fieldName;
        private FieldConstraintsBuilder constraints;

        public FieldBuilderAllowSpecialChars(ParserDefinitionBuilder parserBuilder, CronFieldName fieldName){
            this.parserBuilder = parserBuilder;
            this.fieldName = fieldName;
            this.constraints = FieldConstraintsBuilder.instance().forField(fieldName);
        }

        public FieldBuilderAllowSpecialChars withIntMapping(int source, int dest){
            constraints.withIntValueMapping(source, dest);
            return this;
        }

        public FieldBuilderAllowSpecialChars supportsHash(){
            constraints.addHashSupport();
            return this;
        }

        public FieldBuilderAllowSpecialChars supportsL(){
            constraints.addLSupport();
            return this;
        }

        public FieldBuilderAllowSpecialChars supportsW(){
            constraints.addWSupport();
            return this;
        }

        public ParserDefinitionBuilder and(){
            parserBuilder.register(new CronField(fieldName, constraints.createConstraintsInstance()));
            return parserBuilder;
        }
    }

}
