package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.threeten.bp.ZonedDateTime;
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
class EveryFieldValueGenerator extends FieldValueGenerator {
    private static final Logger log = LoggerFactory.getLogger(EveryFieldValueGenerator.class);

    public EveryFieldValueGenerator(CronField cronField) {
        super(cronField);
        log.trace(String.format(
                "processing \"%s\" at %s",
                cronField.getExpression().asString(), ZonedDateTime.now()
        ));
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        //intuition: for valid values, we have: offset+period*i
        if(reference>=cronField.getConstraints().getEndRange()){
            throw new NoSuchValueException();
        }
        Every every = (Every)cronField.getExpression();
        int referenceWithoutOffset = reference-offset();
        int period = every.getPeriod().getValue();
        int remainder = referenceWithoutOffset % period;

        int next = reference+(period-remainder);
        if(next<cronField.getConstraints().getStartRange()){
            return cronField.getConstraints().getStartRange();
        }
        if(next>cronField.getConstraints().getEndRange()){
            throw new NoSuchValueException();
        }
        return next;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        Every every = (Every)cronField.getExpression();
        int period = every.getPeriod().getValue();
        int remainder = reference % period;
        if(remainder == 0){
            return reference-period;
        }else{
            return reference-remainder;
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer>values = new ArrayList<>();
        try {
            if(start!=offset()){
                values.add(offset());
            }
            int reference = generateNextValue(start);
            while(reference<end){
                values.add(reference);
                reference=generateNextValue(reference);
            }
        } catch (NoSuchValueException ignored) {}//we just skip, since we generate values until we get the exception
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        Every every = (Every)cronField.getExpression();
        int start = offset();
        return ((value-start) % every.getPeriod().getValue()) == 0;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Every;
    }

    @VisibleForTesting
    int offset(){
        FieldExpression expression = ((Every)cronField.getExpression()).getExpression();
        if(expression instanceof On){
            return ((On) expression).getTime().getValue();
        }
        return cronField.getConstraints().getStartRange();
    }
}
