package com.cronutils.model.time;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cronutils.model.field.CronFieldName.DAY_OF_WEEK;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;

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
	private static final Logger log = LoggerFactory.getLogger(ExecutionTime.class);

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
        for(CronFieldName name : CronFieldName.values()){
            if(fields.get(name)!=null){
                switch (name){
                    case SECOND:
                        executionTimeBuilder.forSecondsMatching(fields.get(name));
                        break;
                    case MINUTE:
                        executionTimeBuilder.forMinutesMatching(fields.get(name));
                        break;
                    case HOUR:
                        executionTimeBuilder.forHoursMatching(fields.get(name));
                        break;
                    case DAY_OF_WEEK:
                        executionTimeBuilder.forDaysOfWeekMatching(fields.get(name));
                        break;
                    case DAY_OF_MONTH:
                        executionTimeBuilder.forDaysOfMonthMatching(fields.get(name));
                        break;
                    case MONTH:
                        executionTimeBuilder.forMonthsMatching(fields.get(name));
                        break;
                    case YEAR:
                        executionTimeBuilder.forYearsMatching(fields.get(name));
                        break;
                }
            }
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
        TimeNode days = null;
        int lowestMonth = months.getValues().get(0);
        int lowestHour = hours.getValues().get(0);
        int lowestMinute = minutes.getValues().get(0);
        int lowestSecond = seconds.getValues().get(0);

        NearestValue nearestValue;
        DateTime newDate;
        if(year.isEmpty()){
            int newYear = yearsValueGenerator.generateNextValue(date.getYear());
            days = generateDays(cronDefinition, new DateTime(newYear, lowestMonth, 1, 0, 0));
            return initDateTime(yearsValueGenerator.generateNextValue(date.getYear()), lowestMonth, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if(!months.getValues().contains(date.getMonthOfYear())) {
            nearestValue = months.getNextValue(date.getMonthOfYear(), 0);
            int nextMonths = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), 1, 1, 0, 0, 0, date.getZone()).plusYears(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getMonthOfYear()) {
            	date = date.plusYears(1);
            }
            days = generateDays(cronDefinition, new DateTime(date.getYear(), nextMonths, 1, 0, 0));
            return initDateTime(date.getYear(), nextMonths, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        days = generateDays(cronDefinition, date);
        if(!days.getValues().contains(date.getDayOfMonth())) {
            nearestValue = days.getNextValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, date.getZone()).plusMonths(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getDayOfMonth()) {
            	date = date.plusMonths(1);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), nearestValue.getValue(), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if(!hours.getValues().contains(date.getHourOfDay())) {
            nearestValue = hours.getNextValue(date.getHourOfDay(), 0);
            int nextHours = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(),
                                date.getDayOfMonth(), 0, 0, 0, date.getZone()).plusDays(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getHourOfDay()) {
            	date = date.plusDays(1);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), nextHours, lowestMinute, lowestSecond, date.getZone());
        }
        if(!minutes.getValues().contains(date.getMinuteOfHour())) {
            nearestValue = minutes.getNextValue(date.getMinuteOfHour(), 0);
            int nextMinutes = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
                                0, 0, date.getZone()).plusHours(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getMinuteOfHour()) {
            	date = date.plusHours(1);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), nextMinutes, lowestSecond, date.getZone());
        }
        if(!seconds.getValues().contains(date.getSecondOfMinute())) {
            nearestValue = seconds.getNextValue(date.getSecondOfMinute(), 0);
            int nextSeconds = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(),
                                date.getDayOfMonth(), date.getHourOfDay(),
                                date.getMinuteOfHour(),0, date.getZone()).plusMinutes(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getSecondOfMinute()) {
            	date = date.plusMinutes(1);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(), nextSeconds, date.getZone());
        }
        return date;
    }

    /**
     * If date is not match, will return previous closest match.
     * If date is match, will return this date.
     * @param date - reference DateTime instance - never null;
     * @return DateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    DateTime previousClosestMatch(DateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days = generateDays(cronDefinition, date);
        int highestMonth = months.getValues().get(months.getValues().size()-1);
        int highestDay = days.getValues().get(days.getValues().size()-1);
        int highestHour = hours.getValues().get(hours.getValues().size()-1);
        int highestMinute = minutes.getValues().get(minutes.getValues().size()-1);
        int highestSecond = seconds.getValues().get(seconds.getValues().size()-1);

        NearestValue nearestValue;
        DateTime newDate;
        if(year.isEmpty()){
            int previousYear = yearsValueGenerator.generatePreviousValue(date.getYear());
            if(highestDay>28){
                int highestDayOfMonth = new DateTime(previousYear, highestMonth, 1,0,0).dayOfMonth().getMaximumValue();
                if(highestDay>highestDayOfMonth){
                    nearestValue = days.getPreviousValue(highestDay, 1);
                    if(nearestValue.getShifts()>0){
                        newDate = new DateTime(previousYear, highestMonth, 1, 23, 59, 59, date.getZone())
                                .minusMonths(nearestValue.getShifts()).dayOfMonth().withMaximumValue();
                        return previousClosestMatch(newDate);
                    }else{
                        highestDay = nearestValue.getValue();
                    }
                }
            }
            return initDateTime(previousYear, highestMonth, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!months.getValues().contains(date.getMonthOfYear())){
            nearestValue = months.getPreviousValue(date.getMonthOfYear(), 0);
            int previousMonths = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), 12, 31, 23, 59, 59, date.getZone()).minusYears(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), previousMonths, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!days.getValues().contains(date.getDayOfMonth())){
            nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 23, 59, 59, date.getZone())
                        .minusMonths(nearestValue.getShifts()).dayOfMonth().withMaximumValue();
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), nearestValue.getValue(), highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!hours.getValues().contains(date.getHourOfDay())){
            nearestValue = hours.getPreviousValue(date.getHourOfDay(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(),
                                date.getDayOfMonth(), 23, 59, 59, date.getZone()).minusDays(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), nearestValue.getValue(), highestMinute, highestSecond, date.getZone());
        }
        if(!minutes.getValues().contains(date.getMinuteOfHour())){
            nearestValue = minutes.getPreviousValue(date.getMinuteOfHour(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(),
                                date.getDayOfMonth(), date.getHourOfDay(), 59, 59, date.getZone()).minusHours(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), nearestValue.getValue(), highestSecond, date.getZone());
        }
        if(!seconds.getValues().contains(date.getSecondOfMinute())){
            nearestValue = seconds.getPreviousValue(date.getSecondOfMinute(), 0);
            int previousSeconds = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        new DateTime(date.getYear(), date.getMonthOfYear(),
                                date.getDayOfMonth(), date.getHourOfDay(),
                                date.getMinuteOfHour(), 59, date.getZone()).minusMinutes(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(), previousSeconds, date.getZone());
        }
        return date;
    }

    TimeNode generateDays(CronDefinition cronDefinition, DateTime date){
        boolean questionMarkSupported =
                cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        if(questionMarkSupported){
            return new TimeNode(
                    generateDayCandidatesQuestionMarkSupported(
                            date.getYear(), date.getMonthOfYear(),
                            ((DayOfWeekFieldDefinition)
                                    cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                            ).getMondayDoWValue()
                    )
            );
        }else{
            return new TimeNode(
                    generateDayCandidatesQuestionMarkNotSupported(
                            date.getYear(), date.getMonthOfYear(),
                            ((DayOfWeekFieldDefinition)
                                    cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                            ).getMondayDoWValue()
                    )
            );
        }
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

    /**
     * Provide feedback if a given date matches the cron expression.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return true if date matches cron expression requirements, false otherwise.
     */
    public boolean isMatch(DateTime date){
        return nextExecution(lastExecution(date)).equals(date);
    }

	private List<Integer> generateDayCandidatesQuestionMarkNotSupported(int year, int month, WeekDay mondayDoWValue) {
		DateTime date = new DateTime(year, month, 1, 1, 1);
		Set<Integer> candidates = Sets.newHashSet();
		if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
			candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1,
					date.dayOfMonth().getMaximumValue()));
		} else {
			if (daysOfMonthCronField.getExpression() instanceof Always) {
				candidates.addAll(FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
						.generateCandidates(1, date.dayOfMonth().getMaximumValue()));
			} else {
				if (daysOfWeekCronField.getExpression() instanceof Always) {
					candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(
							1, date.dayOfMonth().getMaximumValue()));
				} else {
					candidates.addAll(FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
							.generateCandidates(1, date.dayOfMonth().getMaximumValue()));
					candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(
							1, date.dayOfMonth().getMaximumValue()));
				}
			}
		}
		List<Integer> candidatesList = Lists.newArrayList(candidates);
		Collections.sort(candidatesList);
		return candidatesList;
	}

    private List<Integer> generateDayCandidatesQuestionMarkSupported(int year, int month, WeekDay mondayDoWValue){
        DateTime date = new DateTime(year, month, 1,1,1);
        Set<Integer> candidates = Sets.newHashSet();
        if(daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always){
            candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
        } else {
            if(daysOfMonthCronField.getExpression() instanceof QuestionMark){
            	// the day of week calculator must get a -1 value to indicate its generating the first value of the month
                candidates.addAll(FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(-1, date.dayOfMonth().getMaximumValue()));
            }else{
                if(daysOfWeekCronField.getExpression() instanceof QuestionMark){
                    candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
                }else{
                    candidates.addAll(FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
                    candidates.addAll(FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, date.dayOfMonth().getMaximumValue()));
                }
            }
        }
        List<Integer> candidatesList = Lists.newArrayList(candidates);
        Collections.sort(candidatesList);
        return candidatesList;
    }

    private DateTime initDateTime(int years, int monthsOfYear, int dayOfMonth,
                                  int hoursOfDay, int minutesOfHour, int secondsOfMinute, DateTimeZone timeZone) {
        DateTime date =
                new DateTime(0, 1, 1, 0, 0, 0, timeZone)
                        .plusYears(years)
                        .plusMonths(monthsOfYear - 1)
                        .plusDays(dayOfMonth - 1)
                        .plusHours(hoursOfDay)
                        .plusMinutes(minutesOfHour)
                        .plusSeconds(secondsOfMinute);
        return ensureSameDate(date, years, monthsOfYear, dayOfMonth,
                hoursOfDay, minutesOfHour, secondsOfMinute, timeZone);
    }

    private DateTime ensureSameDate(DateTime date, int years, int monthsOfYear, int dayOfMonth,
                                    int hoursOfDay, int minutesOfHour, int secondsOfMinute, DateTimeZone timeZone){
        if(date.getSecondOfMinute()!=secondsOfMinute){
            date = date.plusSeconds(secondsOfMinute-date.getSecondOfMinute());
        }
        if(date.getMinuteOfHour()!=minutesOfHour){
            date = date.plusMinutes(minutesOfHour-date.getMinuteOfHour());
        }
        if(date.getHourOfDay()!=hoursOfDay){
            date = date.plusHours(hoursOfDay-date.getHourOfDay());
        }
        if(date.getDayOfMonth()!=dayOfMonth){
            date = date.plusDays(dayOfMonth-date.getDayOfMonth());
        }
        if(date.getMonthOfYear()!=monthsOfYear){
            date = date.plusMonths(monthsOfYear-date.getMonthOfYear());
        }
        if(date.getYear()!=years){
            date = date.plusYears(years-date.getYear());
        }
        return date;
    }
}
