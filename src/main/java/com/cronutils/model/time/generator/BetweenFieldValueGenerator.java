package com.cronutils.model.time.generator;

import com.cronutils.model.field.Between;
import com.cronutils.model.field.FieldExpression;
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
class BetweenFieldValueGenerator extends FieldValueGenerator {

    public BetweenFieldValueGenerator(FieldExpression expression) {
        super(expression);
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        Between between = (Between)expression;
        int candidate = new EveryFieldValueGenerator(between.getEvery()).generateNextValue(reference);

        if(candidate > between.getTo()){
            throw new NoSuchValueException();
        }
        return candidate;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        Between between = (Between)expression;
        int candidate = new EveryFieldValueGenerator(between.getEvery()).generatePreviousValue(reference);

        if(candidate < between.getFrom()){
            throw new NoSuchValueException();
        }
        return candidate;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = Lists.newArrayList();
        //check overlapping ranges: x1 <= y2 && y1 <= x2
        Between between = (Between)expression;
        if(start<=between.getTo() && between.getFrom()<=end){//ranges overlap
            if(between.getTo()<end){
                end=between.getTo();
            }
            if(between.getFrom()>start){
                start=between.getFrom();
            }
            try {
                int reference = generateNextValue(start);
                while(reference<end){
                    values.add(reference);
                    reference = generateNextValue(reference);
                }
            } catch (NoSuchValueException e) {}
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        Between between = (Between)expression;
        if(value >= between.getFrom() && value <= between.getTo()){
            return new EveryFieldValueGenerator(between.getEvery()).isMatch(value);
        }
        return false;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Between;
    }
}
