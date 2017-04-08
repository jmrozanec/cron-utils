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
import com.cronutils.model.time.generator.NoDaysForMonthException;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.threeten.bp.*;
import java.util.*;
import com.cronutils.Function;

import static com.cronutils.model.field.CronFieldName.*;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

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
        this.cronDefinition = Preconditions.checkNotNull(cronDefinition);
        this.yearsValueGenerator = Preconditions.checkNotNull(yearsValueGenerator);
        this.daysOfWeekCronField = Preconditions.checkNotNull(daysOfWeekCronField);
        this.daysOfMonthCronField = Preconditions.checkNotNull(daysOfMonthCronField);
        this.months = Preconditions.checkNotNull(months);
        this.hours = Preconditions.checkNotNull(hours);
        this.minutes = Preconditions.checkNotNull(minutes);
        this.seconds = Preconditions.checkNotNull(seconds);
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
                    default:
                        break;
                }
            }
        }
        return executionTimeBuilder.build();
    }

    /**
     * Provide nearest date for next execution.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional<ZonedDateTime> instance, never null. Contains next execution time or empty.
     */
    public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime nextMatch = nextClosestMatch(date);
            if(nextMatch.equals(date)){
                nextMatch = nextClosestMatch(date.plusSeconds(1));
            }
            return Optional.of(nextMatch);
        } catch (NoSuchValueException e) {
            return Optional.absent();
        }
    }

    /**
     * If date is not match, will return next closest match.
     * If date is match, will return this date.
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    private ZonedDateTime nextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult newdate = new ExecutionTimeResult(date, false);
        do {
            newdate = potentialNextClosestMatch(newdate.getTime());
        } while(!newdate.isMatch());
        return newdate.getTime();
    }

    private ExecutionTimeResult potentialNextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
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
            try {
                days = generateDays(cronDefinition, ZonedDateTime.of(LocalDateTime.of(newYear, lowestMonth, 1, 0, 0), date.getZone()));
            } catch (NoDaysForMonthException e) {
                return new ExecutionTimeResult(date.plusMonths(1), false);
            }
            return initDateTime(yearsValueGenerator.generateNextValue(date.getYear()), lowestMonth, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if(!months.getValues().contains(date.getMonthValue())) {
            nearestValue = months.getNextValue(date.getMonthValue(), 0);
            int nextMonths = nearestValue.getValue();
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0), date.getZone()).plusYears(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getMonthValue()) {
                date = date.plusYears(1);
                return initDateTime(date.getYear(), nextMonths, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
            }
            try {
                days = generateDays(cronDefinition, ZonedDateTime.of(LocalDateTime.of(date.getYear(), nextMonths, 1, 0, 0), date.getZone()));
            } catch (NoDaysForMonthException e) {
                return new ExecutionTimeResult(date.plusMonths(1), false);
            }
            return initDateTime(date.getYear(), nextMonths, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        try {
            days = generateDays(cronDefinition, date);
        } catch (NoDaysForMonthException e) {
            return new ExecutionTimeResult(date.plusMonths(1), false);
        }
        if(!days.getValues().contains(date.getDayOfMonth())) {
            nearestValue = days.getNextValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0), date.getZone()).plusMonths(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getDayOfMonth()) {
                date = date.plusMonths(1);
                return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), lowestHour, lowestMinute, lowestSecond, date.getZone());
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
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getHour()) {
                date = date.plusDays(1);
                return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), nextHours, lowestMinute, lowestSecond, date.getZone());
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
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getMinute()) {
                date = date.plusHours(1);
                return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), nextMinutes, lowestSecond, date.getZone());
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
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getSecond()) {
                date = date.plusMinutes(1);
                return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), nextSeconds, date.getZone());
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), nextSeconds, date.getZone());
        }
        return new ExecutionTimeResult(date, true);
    }

    /**
     * If date is not match, will return previous closest match.
     * If date is match, will return this date.
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    private ZonedDateTime previousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult newdate = new ExecutionTimeResult(date, false);
        do {
            newdate = potentialPreviousClosestMatch(newdate.getTime());
        } while(!newdate.isMatch());
        return newdate.getTime();
    }

    private ExecutionTimeResult potentialPreviousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days = null;
        try {
            days = generateDays(cronDefinition, date);
        } catch (NoDaysForMonthException e) {
            return new ExecutionTimeResult(date.minusMonths(1), false);
        }
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
                        return new ExecutionTimeResult(newDate, false);
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
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), previousMonths, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!days.getValues().contains(date.getDayOfMonth())){
            nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
            if(nearestValue.getShifts()>0){
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 23, 59, 59), date.getZone())
                        .minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), highestHour, highestMinute, highestSecond, date.getZone());
        }
        if(!hours.getValues().contains(date.getHour())){
            nearestValue = hours.getPreviousValue(date.getHour(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), 23, 59, 59), date.getZone()).minusDays(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), nearestValue.getValue(), highestMinute, highestSecond, date.getZone());
        }
        if(!minutes.getValues().contains(date.getMinute())){
            nearestValue = minutes.getPreviousValue(date.getMinute(), 0);
            if(nearestValue.getShifts()>0){
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(), 59, 59), date.getZone()).minusHours(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
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
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), previousSeconds, date.getZone());
        }
        return new ExecutionTimeResult(date, true);
    }

    private TimeNode generateDays(CronDefinition cronDefinition, ZonedDateTime date) throws NoDaysForMonthException {
        //If DoW is not supported in custom definition, we just return an empty list.
        if(cronDefinition.getFieldDefinition(DAY_OF_WEEK)!=null && cronDefinition.getFieldDefinition(DAY_OF_MONTH)!=null){
            return generateDaysDoWAndDoMSupported(cronDefinition, date);
        }
        if(cronDefinition.getFieldDefinition(DAY_OF_WEEK)==null){
            return generateDayCandidatesUsingDoM(date);
        }
        return generateDayCandidatesUsingDoW(date, ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue());
    }

    private TimeNode generateDaysDoWAndDoMSupported(CronDefinition cronDefinition, ZonedDateTime date) throws NoDaysForMonthException {
        boolean questionMarkSupported =
                cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        List<Integer> candidates = new ArrayList<>();
        if(questionMarkSupported){
            candidates = generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(
                    date.getYear(),
                    date.getMonthValue(),
                    ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()
            );
        }else{
            candidates = generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(
                    date.getYear(), date.getMonthValue(),
                    ((DayOfWeekFieldDefinition)
                            cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                    ).getMondayDoWValue()
            );
        }
        if(candidates.isEmpty()){
            throw new NoDaysForMonthException();
        }
        return new TimeNode(candidates);
    }

    /**
     * Provide nearest time for next execution.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time to next execution.
     */
    public Optional<Duration> timeToNextExecution(ZonedDateTime date){
        Optional<ZonedDateTime> next = nextExecution(date);
        if(next.isPresent()){
            return Optional.of(Duration.between(date, next.get()));
        }
        return Optional.absent();
    }

    /**
     * Provide nearest date for last execution.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional<ZonedDateTime> instance, never null. Last execution time or empty.
     */
    public Optional<ZonedDateTime> lastExecution(ZonedDateTime date){
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime previousMatch = previousClosestMatch(date);
            if(previousMatch.equals(date)){
                previousMatch = previousClosestMatch(date.minusSeconds(1));
            }
            return Optional.of(previousMatch);
        } catch (NoSuchValueException e) {
            return Optional.absent();
        }
    }

    /**
     * Provide nearest time from last execution.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time from last execution.
     */
    public Optional<Duration> timeFromLastExecution(ZonedDateTime date){
        Optional<ZonedDateTime> last = lastExecution(date);
        if(last.isPresent()){
            return Optional.of(Duration.between(last.get(), date));
        }
        return Optional.absent();
    }

    /**
     * Provide feedback if a given date matches the cron expression.
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return true if date matches cron expression requirements, false otherwise.
     */
    public boolean isMatch(ZonedDateTime date){
        Optional<ZonedDateTime> last = lastExecution(date);
        if(last.isPresent()){
            Optional<ZonedDateTime> next = nextExecution(last.get());
            if(next.isPresent()){
                return next.get().equals(date);
            }else{
                boolean everythingInRange = false;
                try {
                    everythingInRange = dateValuesInExpectedRanges(nextClosestMatch(date), date);
                } catch (NoSuchValueException ignored) {}
                try {
                    everythingInRange = dateValuesInExpectedRanges(previousClosestMatch(date), date);
                } catch (NoSuchValueException ignored) {}
                return everythingInRange;
            }
        }
        return false;
    }

    private boolean dateValuesInExpectedRanges(ZonedDateTime validCronDate, ZonedDateTime date){
        boolean everythingInRange = true;
        if(cronDefinition.getFieldDefinition(YEAR)!=null){
            everythingInRange = validCronDate.getYear()==date.getYear();
        }
        if(cronDefinition.getFieldDefinition(MONTH)!=null){
            everythingInRange = everythingInRange && validCronDate.getMonthValue()==date.getMonthValue();
        }
        if(cronDefinition.getFieldDefinition(DAY_OF_MONTH)!=null){
            everythingInRange = everythingInRange && validCronDate.getDayOfMonth()==date.getDayOfMonth();
        }
        if(cronDefinition.getFieldDefinition(DAY_OF_WEEK)!=null){
            everythingInRange = everythingInRange && validCronDate.getDayOfWeek().getValue()==date.getDayOfWeek().getValue();
        }
        if(cronDefinition.getFieldDefinition(HOUR)!=null){
            everythingInRange = everythingInRange && validCronDate.getHour()==date.getHour();
        }
        if(cronDefinition.getFieldDefinition(MINUTE)!=null){
            everythingInRange = everythingInRange && validCronDate.getMinute()==date.getMinute();
        }
        if(cronDefinition.getFieldDefinition(SECOND)!=null){
            everythingInRange = everythingInRange && validCronDate.getSecond()==date.getSecond();
        }
        return everythingInRange;
    }

	private List<Integer> generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(int year, int month, WeekDay mondayDoWValue) {
        LocalDate date = LocalDate.of(year, month, 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = new HashSet<>();
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
        List<Integer> candidatesList = new ArrayList<>(candidates);
		Collections.sort(candidatesList);
		return candidatesList;
	}

    private List<Integer> generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(int year, int month, WeekDay mondayDoWValue){
        LocalDate date = LocalDate.of(year, month, 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = new HashSet<>();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        } else if (daysOfMonthCronField.getExpression() instanceof QuestionMark) {
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth));
        } else if (daysOfWeekCronField.getExpression() instanceof QuestionMark) {
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        } else {
            candidates.addAll(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth));
            candidates.addAll(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth));
        }
        List<Integer> candidatesList = new ArrayList<>(candidates);
        Collections.sort(candidatesList);
        return candidatesList;
    }

    private TimeNode generateDayCandidatesUsingDoM(ZonedDateTime reference) {
        LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = new HashSet<>(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, reference.getYear(), reference.getMonthValue()).generateCandidates(1, lengthOfMonth));
        List<Integer> candidatesList = new ArrayList<>(candidates);
        Collections.sort(candidatesList);
        return new TimeNode(candidatesList);
    }

    private TimeNode generateDayCandidatesUsingDoW(ZonedDateTime reference, WeekDay mondayDoWValue){
        LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = new HashSet<>(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, reference.getYear(), reference.getMonthValue(), mondayDoWValue).generateCandidates(0, lengthOfMonth+1));
        List<Integer> candidatesList = new ArrayList<>(candidates);
        Collections.sort(candidatesList);
        return new TimeNode(candidatesList);
    }

    private ExecutionTimeResult initDateTime(int years, int monthsOfYear, int dayOfMonth,
                                  int hoursOfDay, int minutesOfHour, int secondsOfMinute, ZoneId timeZone) throws NoSuchValueException {
        ZonedDateTime date =
                ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0, 0), timeZone)
                        .plusYears(years)
                        .plusMonths(monthsOfYear - 1)
                        .plusDays(dayOfMonth - 1)
                        .plusHours(hoursOfDay)
                        .plusMinutes(minutesOfHour)
                        .plusSeconds(secondsOfMinute);
        ZonedDateTime result = ensureSameDate(date, years, monthsOfYear, dayOfMonth, hoursOfDay, minutesOfHour, secondsOfMinute);
        if(isSameDate(result, years, monthsOfYear, dayOfMonth, hoursOfDay, minutesOfHour, secondsOfMinute)){
            return new ExecutionTimeResult(result, true);
        }
        return new ExecutionTimeResult(result, false);
    }

    private ZonedDateTime ensureSameDate(ZonedDateTime date, int years, int monthsOfYear, int dayOfMonth,
                                    int hoursOfDay, int minutesOfHour, int secondsOfMinute){
        if(date.getSecond()!=secondsOfMinute){
            date = date.plusSeconds(secondsOfMinute-date.getSecond());
        }
        if(date.getMinute()!=minutesOfHour){
            date = date.plusMinutes(minutesOfHour-date.getMinute());
        }
        if(date.getHour()!=hoursOfDay){
            date = date.plusHours(hoursOfDay-date.getHour());
            if(date.getHour()<hoursOfDay){
                //we just switched more hours than required due to daylight savings - we need to move past this change seeking next match
                date = date.plusHours(hoursOfDay-date.getHour());
            }
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

    private boolean isSameDate(ZonedDateTime date, int years, int monthsOfYear, int dayOfMonth,
                                         int hoursOfDay, int minutesOfHour, int secondsOfMinute){
        return date.getSecond()==secondsOfMinute && date.getMinute()==minutesOfHour &&
                date.getHour()==hoursOfDay && date.getDayOfMonth()==dayOfMonth &&
                date.getMonthValue()==monthsOfYear && date.getYear()==years;
    }

    private static class ExecutionTimeResult {
        private ZonedDateTime time;
        private boolean isMatch;

        private ExecutionTimeResult(ZonedDateTime time, boolean isMatch) {
            this.time = time;
            this.isMatch = isMatch;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public boolean isMatch() {
            return isMatch;
        }
    }
}
