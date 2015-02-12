package com.cronutils.model.time.generator;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.*;
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
public class FieldValueGeneratorFactory {
    private static FieldValueGeneratorFactory factory = new FieldValueGeneratorFactory();
    private FieldValueGeneratorFactory(){}

    public static FieldValueGeneratorFactory instance(){
        return factory;
    }

    public static FieldValueGenerator forCronField(CronField cronField){
        FieldExpression fieldExpression = cronField.getExpression();
        if(fieldExpression instanceof Always){
            return new AlwaysFieldValueGenerator(fieldExpression);
        }
        if(fieldExpression instanceof And){
            return new AndFieldValueGenerator(fieldExpression);
        }
        if(fieldExpression instanceof Between){
            return new BetweenFieldValueGenerator(fieldExpression);
        }
        if(fieldExpression instanceof Every){
            return new EveryFieldValueGenerator(fieldExpression);
        }
        if(fieldExpression instanceof On){
            On on = (On) fieldExpression;
            if(!SpecialChar.NONE.equals(on.getSpecialChar())) {
                throw new RuntimeException(String.format("Cannot create instance for On instance with %s value", on.getSpecialChar()));
            }
            return new OnFieldValueGenerator(fieldExpression);
        }
        return new NullFieldValueGenerator(cronField.getExpression());
    }

    public static FieldValueGenerator createDayOfMonthValueGeneratorInstance(CronField cronField, int year, int month){
        FieldExpression fieldExpression = cronField.getExpression();
        if(fieldExpression instanceof On){
            On on = (On) fieldExpression;
            if(!SpecialChar.NONE.equals(on.getSpecialChar())){
                return new OnDayOfMonthValueGenerator(cronField, year, month);
            }
        }
        return forCronField(cronField);
    }

    public static FieldValueGenerator createDayOfWeekValueGeneratorInstance(CronField cronField, int year, int month, WeekDay mondayDoWValue){
        FieldExpression fieldExpression = cronField.getExpression();
        if(fieldExpression instanceof On){
            On on = (On) fieldExpression;
            if(!SpecialChar.NONE.equals(on.getSpecialChar())){
                return new OnDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
            }
        }
        return forCronField(cronField);
    }

}
