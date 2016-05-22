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
    private Always always;
    public AlwaysFieldValueGenerator(CronField cronField) {
        super(cronField);
        always = (Always)cronField.getExpression();
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException{
        return new EveryFieldValueGenerator(new CronField(cronField.getField(), always.getEvery(), cronField.getConstraints())).generateNextValue(reference);
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        return new EveryFieldValueGenerator(new CronField(cronField.getField(), always.getEvery(), cronField.getConstraints())).generatePreviousValue(reference);
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = Lists.newArrayList();
        Always always = (Always)cronField.getExpression();
        int interval = always.getEvery().getTime().getValue();
        for(int j = start+interval; j<end; j+=interval){
            values.add(j);
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        return value%always.getEvery().getTime().getValue()==0;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Always;
    }
}
