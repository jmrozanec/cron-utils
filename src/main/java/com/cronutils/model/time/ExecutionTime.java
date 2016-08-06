package com.cronutils.model.time;

import static com.cronutils.model.field.CronFieldName.DAY_OF_WEEK;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
     * @return ZonedDateTime instance, never null. Next execution time.
     */
    public ZonedDateTime nextExecution(ZonedDateTime date) {
        Validate.notNull(date);
        try {
            ZonedDateTime nextMatch = nextClosestMatch(date);
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
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    ZonedDateTime nextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days = null;
        int lowestMonth = months.getValues().get(0);
        int lowestHour = hours.getValues().get(0);
        int lowestMinute = minutes.getValues().get(0);
        int lowestSecond = seconds.getValues().get(0);

        NearestValue nearestValue;
        ZonedDateTime newDate;
        if(year.isEmpty()){
            int newYear = yearsValueGenerator.generateNextValue(date.getYear());
            days = generateDays(cronDefinition, ZonedDateTime.of(LocalDateTime.of(newYear, lowestMonth, 1, 0, 0), date.getZone()));
            return initDateTime(yearsValueGenerator.generateNextValue(date.getYear()), lowestMonth, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if(!months.getValues().contains(date.getMonthValue())) {
            nearestValue = months.getNextValue(date.getMonthValue(), 0);
            int nextMonths = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0), date.getZone()).plusYears(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getMonthValue()) {
            	date = date.plusYears(1);
            }
            days = generateDays(cronDefinition, ZonedDateTime.of(LocalDateTime.of(date.getYear(), nextMonths, 1, 0, 0), date.getZone()));
            return initDateTime(date.getYear(), nextMonths, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        days = generateDays(cronDefinition, date);
        if(!days.getValues().contains(date.getDayOfMonth())) {
            nearestValue = days.getNextValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0), date.getZone()).plusMonths(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getDayOfMonth()) {
            	date = date.plusMonths(1);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if(!hours.getValues().contains(date.getHour())) {
            nearestValue = hours.getNextValue(date.getHour(), 0);
            int nextHours = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), 0, 0, 0), date.getZone()).plusDays(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getHour()) {
            	date = date.plusDays(1);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), nextHours, lowestMinute, lowestSecond, date.getZone());
        }
        if(!minutes.getValues().contains(date.getMinute())) {
            nearestValue = minutes.getNextValue(date.getMinute(), 0);
            int nextMinutes = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(),
                                0, 0), date.getZone()).plusHours(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getMinute()) {
            	date = date.plusHours(1);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), nextMinutes, lowestSecond, date.getZone());
        }
        if(!seconds.getValues().contains(date.getSecond())) {
            nearestValue = seconds.getNextValue(date.getSecond(), 0);
            int nextSeconds = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(),
                                date.getMinute(),0), date.getZone()).plusMinutes(nearestValue.getShifts());
                return nextClosestMatch(newDate);
            }
            if (nearestValue.getValue() < date.getSecond()) {
            	date = date.plusMinutes(1);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), nextSeconds, date.getZone());
        }
        return date;
    }

    /**
     * If date is not match, will return previous closest match.
     * If date is match, will return this date.
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    ZonedDateTime previousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days = generateDays(cronDefinition, date);
        int highestMonth = months.getValues().get(months.getValues().size()-1);
        int highestDay = days.getValues().get(days.getValues().size()-1);
        int highestHour = hours.getValues().get(hours.getValues().size()-1);
        int highestMinute = minutes.getValues().get(minutes.getValues().size()-1);
        int highestSecond = seconds.getValues().get(seconds.getValues().size()-1);

        NearestValue nearestValue;
        ZonedDateTime newDate;
        if(year.isEmpty()){
            int previousYear = yearsValueGenerator.generatePreviousValue(date.getYear());
            if(highestDay>28){
                int highestDayOfMonth = LocalDate.of(previousYear, highestMonth, 1).lengthOfMonth();
                if(highestDay>highestDayOfMonth){
                    nearestValue = days.getPreviousValue(highestDay, 1);
                    if(nearestValue.getShifts()>0){
                        newDate = ZonedDateTime.of(LocalDateTime.of(previousYear, highestMonth, 1, 23, 59, 59), ZoneId.systemDefault())
                                .minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                        return previousClosestMatch(newDate);
                    }else{
                        highestDay = nearestValue.getValue();
                    }
                }
            }
            return initDateTime(previousYear, highestMonth, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!months.getValues().contains(date.getMonthValue())){
            nearestValue = months.getPreviousValue(date.getMonthValue(), 0);
            int previousMonths = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), 12, 31, 23, 59, 59), date.getZone()).minusYears(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), previousMonths, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!days.getValues().contains(date.getDayOfMonth())){
            nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 23, 59, 59), date.getZone())
                        .minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!hours.getValues().contains(date.getHour())){
            nearestValue = hours.getPreviousValue(date.getHour(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), 23, 59, 59), date.getZone()).minusDays(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), nearestValue.getValue(), highestMinute, highestSecond, date.getZone());
        }
        if(!minutes.getValues().contains(date.getMinute())){
            nearestValue = minutes.getPreviousValue(date.getMinute(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(), 59, 59), date.getZone()).minusHours(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), nearestValue.getValue(), highestSecond, date.getZone());
        }
        if(!seconds.getValues().contains(date.getSecond())){
            nearestValue = seconds.getPreviousValue(date.getSecond(), 0);
            int previousSeconds = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(),
                                date.getMinute(), 59), date.getZone()).minusMinutes(nearestValue.getShifts());
                return previousClosestMatch(newDate);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), previousSeconds, date.getZone());
        }
        return date;
    }

    TimeNode generateDays(CronDefinition cronDefinition, ZonedDateTime date){
        boolean questionMarkSupported =
                cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        if(questionMarkSupported){
            return new TimeNode(
                    generateDayCandidatesQuestionMarkSupported(
                            date.getYear(), date.getMonthValue(),
                            ((DayOfWeekFieldDefinition)
                                    cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                            ).getMondayDoWValue()
                    )
            );
        }else{
            return new TimeNode(
                    generateDayCandidatesQuestionMarkNotSupported(
                            date.getYear(), date.getMonthValue(),
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
    public java.time.Duration timeToNextExecution(ZonedDateTime date){
        return java.time.Duration.between(date, nextExecution(date));
    }

    /**
     * Provide nearest date for last execution.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return ZonedDateTime instance, never null. Last execution time.
     */
    public ZonedDateTime lastExecution(ZonedDateTime date){
        Validate.notNull(date);
        try {
            ZonedDateTime previousMatch = previousClosestMatch(date);
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
    public java.time.Duration timeFromLastExecution(ZonedDateTime date){
        return java.time.Duration.between(lastExecution(date), date);
    }

    /**
     * Provide feedback if a given date matches the cron expression.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return true if date matches cron expression requirements, false otherwise.
     */
    public boolean isMatch(ZonedDateTime date){
        return nextExecution(lastExecution(date)).equals(date);
    }

	private List<Integer> generateDayCandidatesQuestionMarkNotSupported(int year, int month, WeekDay mondayDoWValue) {
        LocalDate date = LocalDate.of(year, month, 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = Sets.newHashSet();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField,
                    year, month).generateCandidates(1, lengthOfMonth));
        } else if (daysOfMonthCronField.getExpression() instanceof Always) {
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, 
                    year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth));
        } else if (daysOfWeekCronField.getExpression() instanceof Always) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField,
                    year, month).generateCandidates(1, lengthOfMonth));
        } else {
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, 
                    year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth));
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        }
        List<Integer> candidatesList = Lists.newArrayList(candidates);
		Collections.sort(candidatesList);
		return candidatesList;
	}

    private List<Integer> generateDayCandidatesQuestionMarkSupported(int year, int month, WeekDay mondayDoWValue){
        LocalDate date = LocalDate.of(year, month, 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = Sets.newHashSet();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        } else if (daysOfMonthCronField.getExpression() instanceof QuestionMark) {
            // the day of week calculator must get a -1 value to indicate its generating the first value of the month
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(-1, lengthOfMonth));
        } else if (daysOfWeekCronField.getExpression() instanceof QuestionMark) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        } else {
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth));
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        }
        List<Integer> candidatesList = Lists.newArrayList(candidates);
        Collections.sort(candidatesList);
        return candidatesList;
    }

    private ZonedDateTime initDateTime(int years, int monthsOfYear, int dayOfMonth,
                                  int hoursOfDay, int minutesOfHour, int secondsOfMinute, ZoneId timeZone) {
        ZonedDateTime date =
                ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0, 0), timeZone)
                        .plusYears(years)
                        .plusMonths(monthsOfYear - 1)
                        .plusDays(dayOfMonth - 1)
                        .plusHours(hoursOfDay)
                        .plusMinutes(minutesOfHour)
                        .plusSeconds(secondsOfMinute);
        return ensureSameDate(date, years, monthsOfYear, dayOfMonth,
                hoursOfDay, minutesOfHour, secondsOfMinute, timeZone);
    }

    private ZonedDateTime ensureSameDate(ZonedDateTime date, int years, int monthsOfYear, int dayOfMonth,
                                    int hoursOfDay, int minutesOfHour, int secondsOfMinute, ZoneId timeZone){
        if(date.getSecond()!=secondsOfMinute){
            date = date.plusSeconds(secondsOfMinute-date.getSecond());
        }
        if(date.getMinute()!=minutesOfHour){
            date = date.plusMinutes(minutesOfHour-date.getMinute());
        }
        if(date.getHour()!=hoursOfDay){
            date = date.plusHours(hoursOfDay-date.getHour());
        }
        if(date.getDayOfMonth()!=dayOfMonth){
            date = date.plusDays(dayOfMonth-date.getDayOfMonth());
        }
        if(date.getMonthValue()!=monthsOfYear){
            date = date.plusMonths(monthsOfYear-date.getMonthValue());
        }
        if(date.getYear()!=years){
            date = date.plusYears(years-date.getYear());
        }
        return date;
    }
}
