package com.cronutils.model.time.generator;

import com.cronutils.model.field.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
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
class AndFieldValueGenerator extends FieldValueGenerator {
    public AndFieldValueGenerator(FieldExpression expression) {
        super(expression);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        List<Integer> candidates =
                computeCandidates(
                        new Function<FieldValueGenerator, Integer>() {
                            @Override
                            public Integer apply(FieldValueGenerator fieldValueGenerator) {
                                try {
                                    return fieldValueGenerator.generateNextValue(reference);
                                } catch (NoSuchValueException e) {
                                    return NO_VALUE;
                                }
                            }
                        }
                );
        if(candidates.isEmpty()){
            throw new NoSuchValueException();
        } else {
            return candidates.get(0);
        }
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        List<Integer> candidates =
                computeCandidates(
                        new Function<FieldValueGenerator, Integer>() {
                            @Override
                            public Integer apply(FieldValueGenerator candidateGenerator) {
                                try {
                                    return candidateGenerator.generatePreviousValue(reference);
                                } catch (NoSuchValueException e) {
                                    return NO_VALUE;
                                }
                            }
                        }
                );
        if(candidates.isEmpty()){
            throw new NoSuchValueException();
        } else {
            return candidates.get(candidates.size() - 1);
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = Lists.newArrayList();
        try {
            int reference = generateNextValue(start);
            while(reference<end){
                values.add(reference);
                reference = generateNextValue(reference);
            }
        } catch (NoSuchValueException e) {}
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        And and = (And) expression;
        boolean match = false;
        for (FieldExpression expression : and.getExpressions()) {
            match = match || createCandidateGeneratorInstance(expression).isMatch(value);
        }
        return match;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof And;
    }

    private List<Integer> computeCandidates(Function<FieldValueGenerator, Integer> function){
        And and = (And) expression;
        List<Integer> candidates = Lists.newArrayList();
        for (FieldExpression expression : and.getExpressions()) {
            candidates.add(function.apply(createCandidateGeneratorInstance(expression)));
        }
        candidates = new ArrayList<Integer>(
                Collections2.filter(candidates, new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer integer) {
                        return integer>=0;
                    }
                })
        );
        Collections.sort(candidates);
        return candidates;
    }

    private FieldValueGenerator createCandidateGeneratorInstance(FieldExpression expression){
        if(expression instanceof Always){
            return new AlwaysFieldValueGenerator(expression);
        }
        if(expression instanceof Between){
            return new BetweenFieldValueGenerator(expression);
        }
        if(expression instanceof Every){
            return new EveryFieldValueGenerator(expression);
        }
        if(expression instanceof On){
            return new OnFieldValueGenerator(expression);
        }
        throw new IllegalArgumentException(String.format("FieldExpression %s not supported!", expression.getClass()));
    }
}
