package com.cronutils.model.time;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
class TimeNode {
	private static final Logger log = LoggerFactory.getLogger(TimeNode.class);
    protected List<Integer> values;

    public TimeNode(List<Integer> values){
        this.values = Validate.notEmpty(values, "Values must not be empty");
        Collections.sort(this.values);
    }

    public NearestValue getNextValue(int reference, int shifts){
        return getNearestForwardValue(reference, shifts);
    }

    public List<Integer> getValues(){
        return Collections.unmodifiableList(values);
    }

    public NearestValue getPreviousValue(int reference, int shifts){
        return getNearestBackwardValue(reference, shifts);
    }

    /**
     * We return same reference value if matches or next one if does not match.
     * Then we start applying shifts.
     * This way we ensure same value is returned if no shift is requested.
     * @param reference - reference value
     * @param shiftsToApply - shifts to apply
     * @return NearestValue instance, never null. Holds information on nearest (forward) value and shifts performed.
     */
    @VisibleForTesting
    NearestValue getNearestForwardValue(int reference, int shiftsToApply){
        List<Integer> values = new ArrayList<Integer>(this.values);
        int index=0;
        boolean foundGreater = false;
        AtomicInteger shift = new AtomicInteger(0);
        if (!values.contains(reference)) {
            for(Integer value : values){
                if(value>reference){
                    index = values.indexOf(value);
                    shiftsToApply--;//we just moved a position!
                    foundGreater = true;
                    break;
                }
            }
            if(!foundGreater){
                shift.incrementAndGet();
            }
        }else{
            index = values.indexOf(reference);
        }
        int value = values.get(index);
        for(int j=0;j<shiftsToApply;j++){
            value = getValueFromList(values, index+1, shift);
            index = values.indexOf(value);
        }
        return new NearestValue(value, shift.get());
    }

    /**
     * We return same reference value if matches or previous one if does not match.
     * Then we start applying shifts.
     * This way we ensure same value is returned if no shift is requested.
     * @param reference - reference value
     * @param shiftsToApply - shifts to apply
     * @return NearestValue instance, never null. Holds information on nearest (backward) value and shifts performed.
     */
    @VisibleForTesting
    NearestValue getNearestBackwardValue(int reference, int shiftsToApply){
        List<Integer> values = new ArrayList<Integer>(this.values);
        Collections.reverse(values);
        int index=0;
        boolean foundSmaller=false;
        AtomicInteger shift = new AtomicInteger(0);
        if (!values.contains(reference)) {
            for(Integer value : values){
                if(value<reference){
                    index = values.indexOf(value);
                    shiftsToApply--;//we just moved a position!
                    foundSmaller = true;
                    break;
                }
            }
            if(!foundSmaller){
                shift.incrementAndGet();
            }
        }else{
            index = values.indexOf(reference);
        }
        int value = values.get(index);
        for(int j=0;j<shiftsToApply;j++){
            value = getValueFromList(values, index+1, shift);
            index = values.indexOf(value);
        }
        return new NearestValue(value, shift.get());
    }

    /**
     * Obtain value from list considering specified index and required shifts
     * @param values - possible values
     * @param index - index to be considered
     * @param shift - shifts that should be applied
     * @return int - required value from values list
     */
    @VisibleForTesting
    int getValueFromList(List<Integer>values, int index, AtomicInteger shift){
        Validate.notEmpty(values, "List must not be empty");
        if(index<0){
            index=index+values.size();
            shift.incrementAndGet();
            return getValueFromList(values, index, shift);
        }
        if(index>=values.size()){
            index=index-values.size();
            shift.incrementAndGet();
            return getValueFromList(values, index, shift);
        }
        return values.get(index);
    }
}
