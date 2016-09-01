package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;

import java.util.ArrayList;
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
class OnFieldValueGenerator extends FieldValueGenerator {
    public OnFieldValueGenerator(CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        int time = ((On) cronField.getExpression()).getTime().getValue();
        if(time<=reference){
            throw new NoSuchValueException();
        }
        return time;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        int time = ((On) cronField.getExpression()).getTime().getValue();
        if(time>=reference){
            throw new NoSuchValueException();
        }
        return time;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = new ArrayList<>();
        int time = ((On) cronField.getExpression()).getTime().getValue();
        if(time>start && time<end){
            values.add(time);
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        return ((On) cronField.getExpression()).getTime().getValue()==value;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }
}
