package com.cronutils.model.time.generator;

import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.google.common.annotations.VisibleForTesting;
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
class EveryFieldValueGenerator extends FieldValueGenerator {

    public EveryFieldValueGenerator(FieldExpression expression) {
        super(expression);
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        //intuition: for valid values, we have: offset+period*i
        if(reference>=expression.getConstraints().getEndRange()){
            throw new NoSuchValueException();
        }
        Every every = (Every)expression;
        int referenceWithoutOffset = reference-offset();
        int period = every.getTime().getValue();
        int remainder = referenceWithoutOffset % period;

        int next = reference+(period-remainder);
        if(next<expression.getConstraints().getStartRange()){
            return expression.getConstraints().getStartRange();
        }
        if(next>expression.getConstraints().getEndRange()){
            throw new NoSuchValueException();
        }
        return next;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        Every every = (Every)expression;
        int period = every.getTime().getValue();
        int remainder = reference % period;
        if(remainder == 0){
            return reference-period;
        }else{
            return reference-remainder;
        }
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
        } catch (NoSuchValueException e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        Every every = (Every)expression;
        int start = every.getConstraints().getStartRange();
        return ((value-start) % every.getTime().getValue()) == 0;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Every;
    }

    @VisibleForTesting
    int offset(){
        return expression.getConstraints().getStartRange();
    }
}
