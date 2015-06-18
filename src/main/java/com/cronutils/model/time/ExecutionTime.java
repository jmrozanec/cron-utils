package com.cronutils.model.time;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class ExecutionTime {
    private CronDefinition cronDefinition;
    private FieldValueGenerator yearsValueGenerator;
    private CronField daysOfWeekCronField;
    private CronField daysOfMonthCronField;

    private TimeNode months;
    private TimeNode hours;
    private TimeNode minutes;
    private TimeNode seconds;

    @VisibleForTesting
    ExecutionTime(CronDefinition cronDefinition, FieldValueGenerator yearsValueGenerator, CronField daysOfWeekCronField,
                  CronField daysOfMonthCronField, TimeNode months, TimeNode hours,
                  TimeNode minutes, TimeNode seconds) {
        this.cronDefinition = Validate.notNull(cronDefinition);
        this.yearsValueGenerator = Validate.notNull(yearsValueGenerator);
        this.daysOfWeekCronField = Validate.notNull(daysOfWeekCronField);
        this.daysOfMonthCronField = Validate.notNull(daysOfMonthCronField);
        this.months = Validate.notNull(months);
        this.hours = Validate.notNull(hours);
        this.minutes = Validate.notNull(minutes);
        this.seconds = Validate.notNull(seconds);
    }

    /**
     * Creates execution time for given Cron
     * @param cron - Cron instance
     * @return ExecutionTime instance
     */
    public static ExecutionTime forCron(Cron cron) {
        Map<CronFieldName, CronField> fields = cron.retrieveFieldsAsMap();
        ExecutionTimeBuilder executionTimeBuilder = new ExecutionTimeBuilder(cron.getCronDefinition());
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
        try {
            DateTime nextMatch = nextClosestMatch(date);
            if(nextMatch.equals(date)){
                nextMatch = nextClosestMatch(date.plusSeconds(1));
            }
            return nextMatch;
        } catch (NoSuchValueException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * If date is not match, will return next closest match.
     * If date is match, will return this date.
     * @param date - reference DateTime instance - never null;
     * @return DateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    DateTime nextClosestMatch(DateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days =
                new TimeNode(
                        generateDayCandidates(
                                date.getYear(), date.getMonthOfYear(),
                                ((DayOfWeekFieldDefinition)
                                        cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)
                                ).getMondayDoWValue()
                        )
                );
        int lowestMonth = months.getValues().get(0);
        int lowestDay = days.getValues().get(0);
        int lowestHour = hours.getValues().get(0);
        int lowestMinute = minutes.getValues().get(0);
        int lowestSecond = seconds.getValues().get(0);

        if(year.isEmpty()){
            return initDateTime(yearsValueGenerator.generateNextValue(date.getYear()), lowestMonth, lowestDay, lowestHour, lowestMinute, lowestSecond);
        }
        if(!months.getValues().contains(date.getMonthOfYear())){
            return initDateTime(date.getYear(), months.getNextValue(date.getMonthOfYear(), 0).getValue(), lowestDay, lowestHour, lowestMinute, lowestSecond);
        }
        if(!days.getValues().contains(date.getDayOfMonth())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), days.getNextValue(date.getDayOfMonth(), 0).getValue(), lowestHour, lowestMinute, lowestSecond);
        }
        if(!hours.getValues().contains(date.getHourOfDay())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), hours.getNextValue(date.getHourOfDay(), 0).getValue(), lowestMinute, lowestSecond);
        }
        if(!minutes.getValues().contains(date.getMinuteOfHour())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), minutes.getNextValue(date.getMinuteOfHour(), 0).getValue(), lowestSecond);
        }
        if(!seconds.getValues().contains(date.getSecondOfMinute())){
            int nextSeconds = seconds.getNextValue(date.getSecondOfMinute(), 0).getValue();
            if(nextSeconds<=date.getSecondOfMinute()){
                return nextClosestMatch(date.plusSeconds(1));
            }else{
                return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(), nextSeconds);
            }
        }
        return date;
    }

    /**
     * If date is not match, will return prevoius closest match.
     * If date is match, will return this date.
     * @param date - reference DateTime instance - never null;
     * @return DateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    DateTime previousClosestMatch(DateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days =
                new TimeNode(
                        generateDayCandidates(
                                date.getYear(), date.getMonthOfYear(),
                                ((DayOfWeekFieldDefinition)
                                        cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)
                                ).getMondayDoWValue()
                        )
                );
        int highestMonth = months.getValues().get(months.getValues().size()-1);
        int highestDay = days.getValues().get(days.getValues().size()-1);
        int highestHour = hours.getValues().get(hours.getValues().size()-1);
        int highestMinute = minutes.getValues().get(minutes.getValues().size()-1);
        int highestSecond = seconds.getValues().get(seconds.getValues().size()-1);

        if(year.isEmpty()){
            return initDateTime(yearsValueGenerator.generatePreviousValue(date.getYear()), highestMonth, highestDay, highestHour, highestMinute, highestSecond);
        }
        if(!months.getValues().contains(date.getMonthOfYear())){
            return initDateTime(date.getYear(), months.getPreviousValue(date.getMonthOfYear(), 0).getValue(), highestDay, highestHour, highestMinute, highestSecond);
        }
        if(!days.getValues().contains(date.getDayOfMonth())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), days.getPreviousValue(date.getDayOfMonth(), 0).getValue(), highestHour, highestMinute, highestSecond);
        }
        if(!hours.getValues().contains(date.getHourOfDay())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), hours.getPreviousValue(date.getHourOfDay(), 0).getValue(), highestMinute, highestSecond);
        }
        if(!minutes.getValues().contains(date.getMinuteOfHour())){
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), minutes.getPreviousValue(date.getMinuteOfHour(), 0).getValue(), highestSecond);
        }
        if(!seconds.getValues().contains(date.getSecondOfMinute())){
            int previousSeconds = seconds.getPreviousValue(date.getSecondOfMinute(), 0).getValue();
            if(previousSeconds>=date.getSecondOfMinute()){
                return previousClosestMatch(date.minusSeconds(1));
            }else{
                return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(), previousSeconds);
            }
        }
        return date;
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
        try {
            DateTime previousMatch = previousClosestMatch(date);
            if(previousMatch.equals(date)){
                previousMatch = previousClosestMatch(date.minusSeconds(1));
            }
            return previousMatch;
        } catch (NoSuchValueException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Provide nearest time from last execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return jodatime Duration instance, never null. Time from last execution.
     */
    public Duration timeFromLastExecution(DateTime date){
        return new Interval(lastExecution(date), date).toDuration();
    }

    private List<Integer> generateDayCandidates(int year, int month, WeekDay mondayDoWValue){
        DateTime date = new DateTime(year, month, 1,1,1);
        Set<Integer> candidates = Sets.newHashSet();
        candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
        candidates.addAll(FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
        List<Integer> candidatesList = Lists.newArrayList(candidates);
        Collections.sort(candidatesList);
        return candidatesList;
    }

    private DateTime initDateTime(int years, int monthsOfYear, int dayOfMonth,
                                  int hoursOfDay, int minutesOfHour, int secondsOfMinute) {
        return new DateTime(0, 1, 1, 0, 0, 0)
                .plusYears(years)
                .plusMonths(monthsOfYear - 1)
                .plusDays(dayOfMonth - 1)
                .plusHours(hoursOfDay)
                .plusMinutes(minutesOfHour)
                .plusSeconds(secondsOfMinute);
    }
}
