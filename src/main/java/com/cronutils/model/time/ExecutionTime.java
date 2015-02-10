package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

/*
 * Copyright 2014 jmrozanec
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
 * Calculates execution time given a cron pattern
 */
class ExecutionTime {
    private FieldValueGenerator yearsValueGenerator;
    private CronField daysOfWeekCronField;
    private CronField daysOfMonthCronField;

    private TimeNode months;
    private TimeNode hours;
    private TimeNode minutes;
    private TimeNode seconds;

    @VisibleForTesting
    ExecutionTime(FieldValueGenerator yearsValueGenerator, CronField daysOfWeekCronField,
                  CronField daysOfMonthCronField, TimeNode months, TimeNode hours,
                  TimeNode minutes, TimeNode seconds) {
        this.yearsValueGenerator = yearsValueGenerator;
        this.daysOfWeekCronField = daysOfWeekCronField;
        this.daysOfMonthCronField = daysOfMonthCronField;
        this.months = months;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Creates execution time for given Cron
     * @param cron - Cron instance
     * @return ExecutionTime instance
     */
    public static ExecutionTime forCron(Cron cron) {
        Map<CronFieldName, CronField> fields = cron.retrieveFieldsAsMap();
        ExecutionTimeBuilder executionTimeBuilder = new ExecutionTimeBuilder();
        if(fields.containsKey(CronFieldName.SECOND)){
            executionTimeBuilder
                    .forSecondsMatching(fields.get(CronFieldName.SECOND));
        }
        executionTimeBuilder
                .forMinutesMatching(fields.get(CronFieldName.MINUTE))
                .forHoursMatching(fields.get(CronFieldName.HOUR))
                .forDaysOfMonthMatching(fields.get(CronFieldName.DAY_OF_MONTH))
                .forDaysOfWeekMatching(fields.get(CronFieldName.DAY_OF_WEEK))
                .forMonthsMatching(fields.get(CronFieldName.MONTH));
        if(fields.containsKey(CronFieldName.YEAR)){
            executionTimeBuilder
                    .forYearsMatching(fields.get(CronFieldName.YEAR));
        }
        return executionTimeBuilder.build();
    }

    /**
     * Provide nearest date for next execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return DateTime instance, never null. Next execution time.
     */
    public DateTime nextExecution(DateTime date) {
        Validate.notNull(date);
        //we request next second value. Ask for a shift, since if matches, we stay at same time as reference time
        NearestValue secondsValue = seconds.getNextValue(date.getSecondOfMinute(), 1);
        //next minute value. If second shifted, lets shift
        NearestValue minutesValue = minutes.getNextValue(date.getMinuteOfHour(), secondsValue.getShifts());
        //next hour value. If minutes shifted, lets shift
        NearestValue hoursValue = hours.getNextValue(date.getHourOfDay(), minutesValue.getShifts());
        NearestValue monthsValue;

        /*
        since days differ from month to month, we need to generate possible days for
        reference month, evaluate, and if we need to switch to next month, days need
        to be re-evaluated.
         */
        int month = 1;
        int day = 1;
        //if current month is contained, we calculate days and try
        if(months.getValues().contains(date.getMonthOfYear())){
            monthsValue = new NearestValue(date.getMonthOfYear(), 0);
            day = date.getDayOfMonth();
        } else {
            //if current month is not contained, get the nearest match,
            // and reset reference day to 1, since first day match in month will be ok
            monthsValue = months.getNextValue(date.getMonthOfYear(), 0);
            month = monthsValue.getValue();
            day = 1;
        }
        TimeNode days = new TimeNode(generateDayCandidates(date.getYear(), month));
        //we evaluate days and apply hour shifts
        NearestValue daysValue = days.getNextValue(day, hoursValue.getShifts());
        //if days requires a new shift, we need to recalculate months and pick first day
        monthsValue = months.getNextValue(month, daysValue.getShifts());
        if(daysValue.getShifts()>0){
            days = new TimeNode(generateDayCandidates(date.getYear(), monthsValue.getValue()));
            //we ask for day candidates and pick the first one. We know candidates are sorted
            daysValue = new NearestValue(days.getValues().get(0), 0);
        }
        //finally calculate years: we take reference and apply month shifts
        NearestValue yearsValue =
                new TimeNode(generateYearCandidates(date.getYear()))
                        .getNextValue(date.getYear(), monthsValue.getShifts());

        return
                new DateTime(
                        yearsValue.getValue(),
                        monthsValue.getValue(),
                        daysValue.getValue(),
                        hoursValue.getValue(),
                        minutesValue.getValue(),
                        secondsValue.getValue()
                );
    }

    /**
     * Provide nearest time for next execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return jodatime Duration instance, never null. Time to next execution.
     */
    public Duration timeToNextExecution(DateTime date){
        return new Interval(date, nextExecution(date)).toDuration();
    }

    /**
     * Provide nearest date for last execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return DateTime instance, never null. Last execution time.
     */
    public DateTime lastExecution(DateTime date){
        Validate.notNull(date);
        //we request previous second value. Ask for a shift, since if matches, we stay at same time as reference time
        NearestValue secondsValue = seconds.getPreviousValue(date.getSecondOfMinute(), 1);
        //prev minute value. If second shifted, lets shift
        NearestValue minutesValue = minutes.getPreviousValue(date.getMinuteOfHour(), secondsValue.getShifts());
        //prev hour value. If minutes shifted, lets shift
        NearestValue hoursValue = hours.getPreviousValue(date.getHourOfDay(), minutesValue.getShifts());
        NearestValue monthsValue;

        /*
        since days differ from month to month, we need to generate possible days for
        reference month, evaluate, and if we need to switch to next month, days need
        to be re-evaluated.
         */
        int month = 1;
        int day = 1;
        //if current month is contained, we calculate days and try
        if(months.getValues().contains(date.getMonthOfYear())){
            monthsValue = new NearestValue(date.getMonthOfYear(), 0);
            day = date.getDayOfMonth();
        } else {
            //if current month is not contained, get the nearest match,
            // and reset reference day to last day in month, since last day match in month will be ok
            monthsValue = months.getPreviousValue(date.getMonthOfYear(), 0);
            month = monthsValue.getValue();
            day = new DateTime(date.getYear(), month, 1, 1, 1).dayOfMonth().withMaximumValue().getDayOfMonth();
        }
        TimeNode days = new TimeNode(generateDayCandidates(date.getYear(), month));
        //we evaluate days and apply hour shifts
        NearestValue daysValue = days.getPreviousValue(day, hoursValue.getShifts());
        //if days requires a new shift, we need to recalculate months and pick last day
        monthsValue = months.getPreviousValue(month, daysValue.getShifts());
        if(daysValue.getShifts()>0){
            days = new TimeNode(generateDayCandidates(date.getYear(), monthsValue.getValue()));
            //we ask for day candidates and pick the last one. We know candidates are sorted
            List<Integer>dayCandidates = days.getValues();
            daysValue = new NearestValue(dayCandidates.get(dayCandidates.size()-1), 0);
        }
        //finally calculate years: we take reference and apply month shifts
        NearestValue yearsValue =
                new TimeNode(generateYearCandidates(date.getYear()))
                        .getPreviousValue(date.getYear(), monthsValue.getShifts());

        return
                new DateTime(
                        yearsValue.getValue(),
                        monthsValue.getValue(),
                        daysValue.getValue(),
                        hoursValue.getValue(),
                        minutesValue.getValue(),
                        secondsValue.getValue()
                );
    }

    /**
     * Provide nearest time from last execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return jodatime Duration instance, never null. Time from last execution.
     */
    public Duration timeFromLastExecution(DateTime date){
        return new Interval(date, lastExecution(date)).toDuration();
    }

    private List<Integer> generateDayCandidates(int year, int month){
        DateTime date = new DateTime(year, month, 1,1,1);
        List<Integer> candidates = Lists.newArrayList();
        candidates.addAll(FieldValueGeneratorFactory.forCronField(daysOfMonthCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
        candidates.addAll(FieldValueGeneratorFactory.forCronField(daysOfWeekCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
        return candidates;
    }

    private List<Integer> generateYearCandidates(int referenceYear){
        List<Integer> candidates = Lists.newArrayList();
        if(yearsValueGenerator.isMatch(referenceYear)){
            candidates.add(referenceYear);
        }
        int highReference = referenceYear;
        int lowReference = referenceYear;
        for(int j=0; j<5; j++){
            try {
                highReference = yearsValueGenerator.generateNextValue(highReference);
                lowReference = yearsValueGenerator.generatePreviousValue(lowReference);
                candidates.add(highReference);
                candidates.add(lowReference);
            } catch (NoSuchValueException e) {}
        }
        return candidates;
    }
}
