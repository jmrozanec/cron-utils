package com.cronutils.model.time.generator;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParserField;
import com.cronutils.utils.Preconditions;

import org.threeten.bp.LocalDate;
import java.util.*;
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

/**
 * This class generates the actual days of month matching the "days of week" specification if the
 * specification is a range like SUN-TUE or MON-FRI. Only a range is supported. It accomplishes this
 * by creating an instance of the OnDayOfWeekValuesGenerator for each day of week needed and then
 * aggregating the values.
 * 
 * The methods:
 *   generateNextValue()
 *   generatePreviousValue()
 *   
 * are not implemented and WILL FAIL logically when called.
 * 
 * @author phethmon
 *
 */
class BetweenDayOfWeekValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;
    private WeekDay mondayDoWValue;
    private Set<Integer> dowValidValues;
    
    public BetweenDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
        this.mondayDoWValue = mondayDoWValue;
        dowValidValues = new HashSet<>();
        Between between = (Between) cronField.getExpression();
        int from = (Integer) between.getFrom().getValue();
        int to = (Integer) between.getTo().getValue();
        while(from<=to){
            dowValidValues.add(from);
            from += 1;
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer>values = new ArrayList<>();
        Between between = (Between) cronField.getExpression();
        
        // we have a range of days of week, so we will generate a list for each day and then combine them
        int startDayOfWeek = 0;
        int endDayOfWeek = 0;
        Object obj = between.getFrom().getValue();
        if (obj instanceof Integer) {
        	Integer i = (Integer)obj;
        	startDayOfWeek = i.intValue();
        }
        obj = between.getTo().getValue();
        if (obj instanceof Integer) {
        	Integer i = (Integer)obj;
        	endDayOfWeek = i.intValue();
        }
        
        for (int i = startDayOfWeek; i <= endDayOfWeek; i++) {
        	// Build a CronField representing a single day of the week
        	FieldConstraintsBuilder fcb = FieldConstraintsBuilder.instance().forField(CronFieldName.DAY_OF_WEEK);
        	CronParserField parser = new CronParserField(CronFieldName.DAY_OF_WEEK, fcb.createConstraintsInstance());
        	CronField cronField =  parser.parse(Integer.toString(i));
        	
        	// now a generator for matching days
        	OnDayOfWeekValueGenerator odow = new OnDayOfWeekValueGenerator(cronField,  year, month, mondayDoWValue);
        	
        	// get the list of matching days
        	List<Integer> candidatesList = odow.generateCandidates(start, end);
        	
        	// add them to the master list
        	if (candidatesList != null) {
        		values.addAll(candidatesList);
        	}
        }
        Collections.sort(values);
        return values;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof Between;
    }

	@Override
	public int generateNextValue(int reference) throws NoSuchValueException {
		// This method does not logically work.
		return 0;
	}

	@Override
	public int generatePreviousValue(int reference) throws NoSuchValueException {
		// This method does not logically work.
		return 0;
	}

	@Override
	public boolean isMatch(int value) {
        return dowValidValues.contains(LocalDate.of(year, month, value).getDayOfWeek().getValue());
	}
}
