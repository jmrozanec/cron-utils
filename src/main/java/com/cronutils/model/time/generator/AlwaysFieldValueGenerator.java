package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.FieldExpression;
import com.google.common.collect.Lists;

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
class AlwaysFieldValueGenerator extends FieldValueGenerator {
    public AlwaysFieldValueGenerator(CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException{
        int newvalue = reference+1;
        if(newvalue<=cronField.getConstraints().getEndRange()){
            return newvalue;
        }else {
            throw new NoSuchValueException();
        }
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        int newvalue = reference-1;
        if(newvalue>=cronField.getConstraints().getStartRange()){
            return newvalue;
        }else {
            throw new NoSuchValueException();
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = Lists.newArrayList();
        for(int j = start+1; j<end; j++){
            values.add(j);
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        return cronField.getConstraints().isInRange(value);
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Always;
    }
}
