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
package com.cronutils.model.field.expression;

import com.cronutils.mapper.ConstantsMapper;
import com.cronutils.mapper.WeekDay;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import java.util.List;

public class FieldExpressionFactory {
    private WeekDay definitionWeekDay;
    private WeekDay enumWeekDay = new WeekDay(Weekdays.MONDAY.getWeekday(), false);

    public FieldExpressionFactory(CronDefinition cronDefinition) {
        this.definitionWeekDay = ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue();
    }
    
    public static Always always(){
        return new Always();
    }

    public Between between(Weekdays from, Weekdays to){
        int fromint = ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, from.getWeekday());
        int toint = ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, to.getWeekday());
        return between(fromint, toint);
    }

    public Between between(SpecialChar from, Weekdays to){
        return between(from, ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, to.getWeekday()));
    }

    public static Between between(int from, int to){
        return new Between(new IntegerFieldValue(from), new IntegerFieldValue(to));
    }

    public static Between between(SpecialChar from, int to){
        return new Between(new SpecialCharFieldValue(from), new IntegerFieldValue(to));
    }

    public static Every every(int time){
        return new Every(new IntegerFieldValue(time));
    }

    public static Every every(FieldExpression expression, int time){
        return new Every(expression, new IntegerFieldValue(time));
    }

    public static On on(SpecialChar specialChar){
        return new On(new SpecialCharFieldValue(specialChar));
    }

    public static On on(int time){
        return new On(new IntegerFieldValue(time));
    }

    public On on(Weekdays weekday){
        return on(ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, weekday.getWeekday()));
    }

    public static On on(int time, SpecialChar specialChar){
        return new On(new IntegerFieldValue(time), new SpecialCharFieldValue(specialChar));
    }

    public On on(Weekdays weekday, SpecialChar specialChar){
        return on(ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, weekday.getWeekday()), specialChar);
    }

    public static On on(int time, SpecialChar specialChar, int nth){
        return new On(new IntegerFieldValue(time), new SpecialCharFieldValue(specialChar), new IntegerFieldValue(nth));
    }

    public On on(Weekdays weekday, SpecialChar specialChar, int nth){
        return on(ConstantsMapper.weekDayMapping(enumWeekDay, definitionWeekDay, weekday.getWeekday()), specialChar, nth);
    }

    public static QuestionMark questionMark(){
        return new QuestionMark();
    }

    public static And and(List<FieldExpression> expressions){
        And and = new And();
        for(FieldExpression expression : expressions){
            and.and(expression);
        }
        return and;
    }
}
