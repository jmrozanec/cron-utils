package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.On;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;

import java.util.List;
import java.util.NoSuchElementException;
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
class OnDayOfMonthValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;
    public OnDayOfMonthValueGenerator(CronField cronField, int year, int month) {
        super(cronField.getExpression());
        Validate.isTrue(CronFieldName.DAY_OF_MONTH.equals(cronField.getField()));
        this.year = year;
        this.month = month;
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        On on = ((On)expression);
        int value = generateValue(on, year, month);

        if(value<=reference){
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        On on = ((On)expression);
        int value = generateValue(on, year, month);
        if(value>=reference){
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer>values = Lists.newArrayList();
        try {
            int reference = generateNextValue(start);
            while(reference<end){
                values.add(reference);
                reference=generateNextValue(reference);
            }
        } catch (NoSuchValueException e) {}
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        On on = ((On)expression);
        try {
            return value == generateValue(on, year, month);
        } catch (NoSuchValueException e) {}
        return false;
    }

    private int generateValue(On on, int year, int month) throws NoSuchValueException {
        switch (on.getSpecialChar()){
            case L:
                return new DateTime(year, month, 1, 1, 1).dayOfMonth().getMaximumValue();
            case W:
                DateTime doM = new DateTime(year, month, on.getTime(), 1, 1);
                if(doM.getDayOfWeek()==6){//dayOfWeek is Saturday!
                    if(on.getTime()==1){//first day in month is Saturday! We execute on Monday
                        return 3;
                    }
                    return on.getTime()-1;
                }
                if(doM.getDayOfWeek()==7){
                    if((on.getTime()+1)<=doM.dayOfMonth().getMaximumValue()){
                        return on.getTime()+1;
                    }
                }
                break;
        }
        throw new NoSuchElementException ();//TODO replace for declared expression
    }


}
