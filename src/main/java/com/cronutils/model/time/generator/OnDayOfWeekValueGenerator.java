package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.FieldExpression;
import com.cronutils.model.field.On;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;

import java.util.List;
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
class OnDayOfWeekValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;
    public OnDayOfWeekValueGenerator(CronField cronField, int year, int month) {
        super(cronField.getExpression());
        Validate.isTrue(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException{
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

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }

    private int generateValue(On on, int year, int month) throws NoSuchValueException {
        switch (on.getSpecialChar()){
            case HASH:
                return generateHashValues(on, year, month);
            case L:
                return generateLValues(on, year, month);
            case NONE:
                return on.getTime();
        }
        throw new NoSuchValueException();
    }

    private int generateHashValues(On on, int year, int month){
        int dowForFirstDoM = new DateTime(year, month, 1, 1, 1).getDayOfWeek();//1-7
        int requiredDoW = on.getTime();//0-6
        int requiredNth = on.getNth();
        int baseDay = 1;//day 1 from given month
        if((requiredDoW + 1) < dowForFirstDoM){
            baseDay += (dowForFirstDoM - (requiredDoW + 1));
        }else{
            baseDay += (requiredDoW - dowForFirstDoM);
        }

        return (requiredNth - 1) * 7 + baseDay;
    }

    private int generateLValues(On on, int year, int month) throws NoSuchValueException {
        int lastDoM = new DateTime(year, month, 1, 1, 1).dayOfMonth().getMaximumValue();
        DateTime lastDoMDateTime = new DateTime(year, month, lastDoM, 1, 1);
        int dowForLastDoM = lastDoMDateTime.getDayOfWeek();//1-7
        int requiredDoW = on.getTime()+1;//(0-6)+1 to normalize to joda-time value
        int dowDiff = dowForLastDoM - requiredDoW;
        if(dowDiff==0){
            return lastDoMDateTime.dayOfMonth().get();//TODO we have the date. check reference!
        }
        if(dowDiff<0){
            return lastDoMDateTime.minusDays((requiredDoW+(7-dowForLastDoM))).dayOfMonth().get();//TODO we have the date. check reference!
        }
        if(dowDiff>0){
            return lastDoMDateTime.minusDays(dowDiff).dayOfMonth().get();//TODO we have the date. check reference!
        }
        throw new NoSuchValueException();
    }
}
