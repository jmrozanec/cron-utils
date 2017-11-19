package com.cronutils.model.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import static com.cronutils.model.field.CronFieldName.DAY_OF_MONTH;
import static com.cronutils.model.field.CronFieldName.DAY_OF_WEEK;
import static com.cronutils.model.field.CronFieldName.DAY_OF_YEAR;
import static com.cronutils.model.field.CronFieldName.HOUR;
import static com.cronutils.model.field.CronFieldName.MINUTE;
import static com.cronutils.model.field.CronFieldName.MONTH;
import static com.cronutils.model.field.CronFieldName.SECOND;
import static com.cronutils.model.field.CronFieldName.YEAR;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance;
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.createDayOfYearValueGeneratorInstance;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

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
 * Calculates execution time given a cron pattern.
 */
public class ExecutionTime {
    private CronDefinition cronDefinition;
    private FieldValueGenerator yearsValueGenerator;
    private CronField daysOfWeekCronField;
    private CronField daysOfMonthCronField;
    private CronField daysOfYearCronField;

    private TimeNode months;
    private TimeNode hours;
    private TimeNode minutes;
    private TimeNode seconds;

    @VisibleForTesting
    ExecutionTime(CronDefinition cronDefinition, FieldValueGenerator yearsValueGenerator, CronField daysOfWeekCronField,
            CronField daysOfMonthCronField, CronField daysOfYearCronField, TimeNode months, TimeNode hours,
            TimeNode minutes, TimeNode seconds) {
        this.cronDefinition = Preconditions.checkNotNull(cronDefinition);
        this.yearsValueGenerator = Preconditions.checkNotNull(yearsValueGenerator);
        this.daysOfWeekCronField = Preconditions.checkNotNull(daysOfWeekCronField);
        this.daysOfMonthCronField = Preconditions.checkNotNull(daysOfMonthCronField);
        this.daysOfYearCronField = daysOfYearCronField;
        this.months = Preconditions.checkNotNull(months);
        this.hours = Preconditions.checkNotNull(hours);
        this.minutes = Preconditions.checkNotNull(minutes);
        this.seconds = Preconditions.checkNotNull(seconds);
    }

    /**
     * Creates execution time for given Cron.
     *
     * @param cron - Cron instance
     * @return ExecutionTime instance
     */
    public static ExecutionTime forCron(Cron cron) {
        Map<CronFieldName, CronField> fields = cron.retrieveFieldsAsMap();
        ExecutionTimeBuilder executionTimeBuilder = new ExecutionTimeBuilder(cron.getCronDefinition());
        for (CronFieldName name : CronFieldName.values()) {
            if (fields.get(name) != null) {
                switch (name) {
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
                    case DAY_OF_YEAR:
                        executionTimeBuilder.forDaysOfYearMatching(fields.get(name));
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
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Contains next execution time or empty.
     */
    public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime nextMatch = nextClosestMatch(date);
            if (nextMatch.equals(date)) {
                nextMatch = nextClosestMatch(date.plusSeconds(1));
            }
            return Optional.of(nextMatch);
        } catch (NoSuchValueException e) {
            return Optional.empty();
        }
    }

    /**
     * If date is not match, will return next closest match.
     * If date is match, will return this date.
     *
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    private ZonedDateTime nextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult newdate = new ExecutionTimeResult(date, false);
        do {
            newdate = potentialNextClosestMatch(newdate.getTime());
        } while (!newdate.isMatch());
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
        if (year.isEmpty()) {
            int newYear = yearsValueGenerator.generateNextValue(date.getYear());
            try {
                days = generateDays(cronDefinition, ZonedDateTime.of(LocalDateTime.of(newYear, lowestMonth, 1, 0, 0), date.getZone()));
            } catch (NoDaysForMonthException e) {
                return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
            }
            return initDateTime(yearsValueGenerator.generateNextValue(date.getYear()), lowestMonth, days.getValues().get(0), lowestHour, lowestMinute,
                    lowestSecond, date.getZone());
        }
        if (!months.getValues().contains(date.getMonthValue())) {
            nearestValue = months.getNextValue(date.getMonthValue(), 0);
            int nextMonths = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
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
                return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
            }
            return initDateTime(date.getYear(), nextMonths, days.getValues().get(0), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        try {
            days = generateDays(cronDefinition, date);
        } catch (NoDaysForMonthException e) {
            return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
        }
        if (!days.getValues().contains(date.getDayOfMonth())) {
            nearestValue = days.getNextValue(date.getDayOfMonth(), 0);
            if (nearestValue.getShifts() > 0) {
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0), date.getZone())
                        .plusMonths(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getDayOfMonth()) {
                date = date.plusMonths(1);
                return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), lowestHour, lowestMinute, lowestSecond, date.getZone());
            }
            return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), lowestHour, lowestMinute, lowestSecond, date.getZone());
        }
        if (!hours.getValues().contains(date.getHour())) {
            nearestValue = hours.getNextValue(date.getHour(), 0);
            int nextHours = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
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
        if (!minutes.getValues().contains(date.getMinute())) {
            nearestValue = minutes.getNextValue(date.getMinute(), 0);
            int nextMinutes = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
                newDate =
                        ZonedDateTime.ofLocal(LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(),
                                0, 0), date.getZone(), date.getOffset()).plusHours(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getMinute()) {
                date = date.plusHours(1);
            }
            newDate = ZonedDateTime.ofLocal(
                    LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), nextMinutes, lowestSecond),
                    date.getZone(), date.getOffset());
            return new ExecutionTimeResult(newDate, true);
        }
        if (!seconds.getValues().contains(date.getSecond())) {
            nearestValue = seconds.getNextValue(date.getSecond(), 0);
            int nextSeconds = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
                newDate =
                        ZonedDateTime.ofLocal(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(),
                                date.getMinute(), 0), date.getZone(), date.getOffset()).plusMinutes(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            if (nearestValue.getValue() < date.getSecond()) {
                date = date.plusMinutes(1);
            }
            newDate = ZonedDateTime.ofLocal(
                    LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), nextSeconds),
                    date.getZone(), date.getOffset());
            return new ExecutionTimeResult(newDate, true);
        }
        return new ExecutionTimeResult(date, true);
    }

    private ZonedDateTime toBeginOfNextMonth(final ZonedDateTime datetime) {
        final ZonedDateTime nextMonth = datetime.plusMonths(1);
        final int lowestHour = hours.getValues().get(0);
        final int lowestMinute = minutes.getValues().get(0);
        final int lowestSecond = seconds.getValues().get(0);
        return ZonedDateTime.of(nextMonth.getYear(), nextMonth.getMonth().getValue(), 1, lowestHour, lowestMinute, lowestSecond, 0, nextMonth.getZone());
    }

    /**
     * If date is not match, will return previous closest match.
     * If date is match, will return this date.
     *
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException
     */
    private ZonedDateTime previousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult newdate = new ExecutionTimeResult(date, false);
        do {
            newdate = potentialPreviousClosestMatch(newdate.getTime());
        } while (!newdate.isMatch());
        return newdate.getTime();
    }

    private ExecutionTimeResult potentialPreviousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
        List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        TimeNode days = null;
        try {
            days = generateDays(cronDefinition, date);
        } catch (NoDaysForMonthException e) {
            return new ExecutionTimeResult(toEndOfPreviousMonth(date), false);
        }
        int highestMonth = months.getValues().get(months.getValues().size() - 1);
        int highestDay = days.getValues().get(days.getValues().size() - 1);
        int highestHour = hours.getValues().get(hours.getValues().size() - 1);
        int highestMinute = minutes.getValues().get(minutes.getValues().size() - 1);
        int highestSecond = seconds.getValues().get(seconds.getValues().size() - 1);

        NearestValue nearestValue;
        ZonedDateTime newDate;
        if (year.isEmpty()) {
            int previousYear = yearsValueGenerator.generatePreviousValue(date.getYear());
            if (highestDay > 28) {
                int highestDayOfMonth = LocalDate.of(previousYear, highestMonth, 1).lengthOfMonth();
                if (highestDay > highestDayOfMonth) {
                    nearestValue = days.getPreviousValue(highestDay, 1);
                    if (nearestValue.getShifts() > 0) {
                        newDate = ZonedDateTime.of(LocalDateTime.of(previousYear, highestMonth, 1, 23, 59, 59), ZoneId.systemDefault())
                                .minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                        return new ExecutionTimeResult(newDate, false);
                    } else {
                        highestDay = nearestValue.getValue();
                    }
                }
            }
            return initDateTime(previousYear, highestMonth, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if (!months.getValues().contains(date.getMonthValue())) {
            nearestValue = months.getPreviousValue(date.getMonthValue(), 0);
            int previousMonths = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), 12, 31, 23, 59, 59), date.getZone()).minusYears(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), previousMonths, highestDay, highestHour, highestMinute, highestSecond, date.getZone());
        }
        if (!days.getValues().contains(date.getDayOfMonth())) {
            nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
            if (nearestValue.getShifts() > 0) {
                newDate = ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 23, 59, 59), date.getZone())
                        .minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), nearestValue.getValue(), highestHour, highestMinute, highestSecond, date.getZone());
        }
        if (!hours.getValues().contains(date.getHour())) {
            nearestValue = hours.getPreviousValue(date.getHour(), 0);
            if (nearestValue.getShifts() > 0) {
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), 23, 59, 59), date.getZone()).minusDays(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), nearestValue.getValue(), highestMinute, highestSecond,
                    date.getZone());
        }
        if (!minutes.getValues().contains(date.getMinute())) {
            nearestValue = minutes.getPreviousValue(date.getMinute(), 0);
            if (nearestValue.getShifts() > 0) {
                newDate =
                        ZonedDateTime.of(LocalDateTime.of(date.getYear(), date.getMonthValue(),
                                date.getDayOfMonth(), date.getHour(), 59, 59), date.getZone()).minusHours(nearestValue.getShifts());
                return new ExecutionTimeResult(newDate, false);
            }
            return initDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), nearestValue.getValue(), highestSecond,
                    date.getZone());
        }
        if (!seconds.getValues().contains(date.getSecond())) {
            nearestValue = seconds.getPreviousValue(date.getSecond(), 0);
            int previousSeconds = nearestValue.getValue();
            if (nearestValue.getShifts() > 0) {
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

    private ZonedDateTime toEndOfPreviousMonth(final ZonedDateTime datetime) {
        final ZonedDateTime previousMonth = datetime.minusMonths(1).with(lastDayOfMonth());
        int highestHour = hours.getValues().get(hours.getValues().size() - 1);
        int highestMinute = minutes.getValues().get(minutes.getValues().size() - 1);
        int highestSecond = seconds.getValues().get(seconds.getValues().size() - 1);
        return ZonedDateTime
                .of(previousMonth.getYear(), previousMonth.getMonth().getValue(), previousMonth.getDayOfMonth(), highestHour, highestMinute, highestSecond, 0,
                        previousMonth.getZone());
    }

    private TimeNode generateDays(CronDefinition cronDefinition, ZonedDateTime date) throws NoDaysForMonthException {
        if (isGenerateDaysAsDoY(cronDefinition)) {
            return generateDayCandidatesUsingDoY(date);
        }
        //If DoW is not supported in custom definition, we just return an empty list.
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) != null && cronDefinition.getFieldDefinition(DAY_OF_MONTH) != null) {
            return generateDaysDoWAndDoMSupported(cronDefinition, date);
        }
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) == null) {
            return generateDayCandidatesUsingDoM(date);
        }
        return generateDayCandidatesUsingDoW(date, ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue());
    }

    private boolean isGenerateDaysAsDoY(CronDefinition cronDefinition) {
        if (!cronDefinition.containsFieldDefinition(DAY_OF_YEAR)) {
            return false;
        }

        if (!cronDefinition.getFieldDefinition(DAY_OF_YEAR).getConstraints().getSpecialChars().contains(QUESTION_MARK)) {
            return true;
        }

        return !(daysOfYearCronField.getExpression() instanceof QuestionMark);
    }

    private TimeNode generateDayCandidatesUsingDoY(ZonedDateTime reference) throws NoDaysForMonthException {
        final int year = reference.getYear();
        final int month = reference.getMonthValue();
        LocalDate date = LocalDate.of(year, 1, 1);
        int lengthOfYear = date.lengthOfYear();

        List<Integer> candidates = createDayOfYearValueGeneratorInstance(daysOfYearCronField, year).generateCandidates(1, lengthOfYear);

        int low = LocalDate.of(year, month, 1).getDayOfYear();
        int high = month == 12
                ? LocalDate.of(year, 12, 31).getDayOfYear() + 1
                : LocalDate.of(year, month + 1, 1).getDayOfYear();

        List<Integer> collectedCandidates = candidates.stream().filter(dayOfYear -> dayOfYear >= low && dayOfYear < high)
                .map(dayOfYear -> LocalDate.ofYearDay(reference.getYear(), dayOfYear).getDayOfMonth())
                .collect(Collectors.toList());

        if (collectedCandidates.isEmpty()) {
            //TODO try to avoid programming by exception, maybe we should better return Optional<TimeNode> and test on presence
            throw new NoDaysForMonthException();
        }

        return new TimeNode(collectedCandidates);
    }

    private TimeNode generateDaysDoWAndDoMSupported(CronDefinition cronDefinition, ZonedDateTime date) throws NoDaysForMonthException {
        boolean questionMarkSupported =
                cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        List<Integer> candidates = new ArrayList<>();
        if (questionMarkSupported) {
            candidates = generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(
                    date.getYear(),
                    date.getMonthValue(),
                    ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()
            );
        } else {
            candidates = generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(
                    date.getYear(), date.getMonthValue(),
                    ((DayOfWeekFieldDefinition)
                            cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                    ).getMondayDoWValue()
            );
        }
        if (candidates.isEmpty()) {
            throw new NoDaysForMonthException();
        }
        return new TimeNode(candidates);
    }

    /**
     * Provide nearest time for next execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time to next execution.
     */
    public Optional<Duration> timeToNextExecution(ZonedDateTime date) {
        Optional<ZonedDateTime> next = nextExecution(date);

        return next.map(zonedDateTime -> Duration.between(date, zonedDateTime));
    }

    /**
     * Provide nearest date for last execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Last execution time or empty.
     */
    public Optional<ZonedDateTime> lastExecution(ZonedDateTime date) {
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime previousMatch = previousClosestMatch(date);
            if (previousMatch.equals(date)) {
                previousMatch = previousClosestMatch(date.minusSeconds(1));
            }
            return Optional.of(previousMatch);
        } catch (NoSuchValueException e) {
            return Optional.empty();
        }
    }

    /**
     * Provide nearest time from last execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time from last execution.
     */
    public Optional<Duration> timeFromLastExecution(ZonedDateTime date) {
        Optional<ZonedDateTime> last = lastExecution(date);

        return last.map(zonedDateTime -> Duration.between(zonedDateTime, date));

    }

    /**
     * Provide feedback if a given date matches the cron expression.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return true if date matches cron expression requirements, false otherwise.
     */
    public boolean isMatch(ZonedDateTime date) {
        // Issue #200: Truncating the date to the least granular precision supported by different cron systems.
        // For Quartz, it's seconds while for Unix & Cron4J it's minutes.
        boolean isSecondGranularity = cronDefinition.containsFieldDefinition(SECOND);
        if (isSecondGranularity) {
            date = date.truncatedTo(ChronoUnit.SECONDS);
        } else {
            date = date.truncatedTo(ChronoUnit.MINUTES);
        }

        Optional<ZonedDateTime> last = lastExecution(date);
        if (last.isPresent()) {
            Optional<ZonedDateTime> next = nextExecution(last.get());
            if (next.isPresent()) {
                return next.get().equals(date);
            } else {
                boolean everythingInRange = false;
                try {
                    everythingInRange = dateValuesInExpectedRanges(nextClosestMatch(date), date);
                } catch (NoSuchValueException ignored) {
                    // Why is this ignored?
                }
                try {
                    everythingInRange = dateValuesInExpectedRanges(previousClosestMatch(date), date);
                } catch (NoSuchValueException ignored) {
                    // Why is this ignored?
                }
                return everythingInRange;
            }
        }
        return false;
    }

    private boolean dateValuesInExpectedRanges(ZonedDateTime validCronDate, ZonedDateTime date) {
        boolean everythingInRange = true;
        if (cronDefinition.getFieldDefinition(YEAR) != null) {
            everythingInRange = validCronDate.getYear() == date.getYear();
        }
        if (cronDefinition.getFieldDefinition(MONTH) != null) {
            everythingInRange = everythingInRange && validCronDate.getMonthValue() == date.getMonthValue();
        }
        if (cronDefinition.getFieldDefinition(DAY_OF_MONTH) != null) {
            everythingInRange = everythingInRange && validCronDate.getDayOfMonth() == date.getDayOfMonth();
        }
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) != null) {
            everythingInRange = everythingInRange && validCronDate.getDayOfWeek().getValue() == date.getDayOfWeek().getValue();
        }
        if (cronDefinition.getFieldDefinition(HOUR) != null) {
            everythingInRange = everythingInRange && validCronDate.getHour() == date.getHour();
        }
        if (cronDefinition.getFieldDefinition(MINUTE) != null) {
            everythingInRange = everythingInRange && validCronDate.getMinute() == date.getMinute();
        }
        if (cronDefinition.getFieldDefinition(SECOND) != null) {
            everythingInRange = everythingInRange && validCronDate.getSecond() == date.getSecond();
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
            List<Integer> dayOfWeekCandidates = createDayOfWeekValueGeneratorInstance(daysOfWeekCronField,
                    year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth);
            List<Integer> dayOfMonthCandidates = createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth);
            if (cronDefinition.isMatchDayOfWeekAndDayOfMonth()) {
                Set<Integer> intersection = new HashSet<>(dayOfWeekCandidates);
                intersection.retainAll(dayOfMonthCandidates);
                candidates.addAll(intersection);
            } else {
                candidates.addAll(dayOfWeekCandidates);
                candidates.addAll(dayOfMonthCandidates);
            }
        }
        List<Integer> candidatesList = new ArrayList<>(candidates);
        Collections.sort(candidatesList);
        return candidatesList;
    }

    private List<Integer> generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(int year, int month, WeekDay mondayDoWValue) {
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
        Set<Integer> candidates = new HashSet<>(createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, reference.getYear(), reference.getMonthValue())
                .generateCandidates(1, lengthOfMonth));
        List<Integer> candidatesList = new ArrayList<>(candidates);
        Collections.sort(candidatesList);
        return new TimeNode(candidatesList);
    }

    private TimeNode generateDayCandidatesUsingDoW(ZonedDateTime reference, WeekDay mondayDoWValue) {
        LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        int lengthOfMonth = date.lengthOfMonth();
        Set<Integer> candidates = new HashSet<>(createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, reference
                .getYear(), reference.getMonthValue(), mondayDoWValue).generateCandidates(1, lengthOfMonth));
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
        if (isSameDate(result, years, monthsOfYear, dayOfMonth, hoursOfDay, minutesOfHour, secondsOfMinute)) {
            return new ExecutionTimeResult(result, true);
        }
        return new ExecutionTimeResult(result, false);
    }

    private ZonedDateTime ensureSameDate(ZonedDateTime date, int years, int monthsOfYear, int dayOfMonth,
            int hoursOfDay, int minutesOfHour, int secondsOfMinute) {
        if (date.getSecond() != secondsOfMinute) {
            date = date.plusSeconds(secondsOfMinute - date.getSecond());
        }
        if (date.getMinute() != minutesOfHour) {
            date = date.plusMinutes(minutesOfHour - date.getMinute());
        }
        if (date.getHour() != hoursOfDay) {
            date = date.plusHours(hoursOfDay - date.getHour());
            if (date.getHour() < hoursOfDay) {
                //we just switched more hours than required due to daylight savings - we need to move past this change seeking next match
                date = date.plusHours(hoursOfDay - date.getHour());
            }
        }
        if (date.getDayOfMonth() != dayOfMonth) {
            date = date.plusDays(dayOfMonth - date.getDayOfMonth());
        }
        if (date.getMonthValue() != monthsOfYear) {
            date = date.plusMonths(monthsOfYear - date.getMonthValue());
        }
        if (date.getYear() != years) {
            date = date.plusYears(years - date.getYear());
        }
        return date;
    }

    private boolean isSameDate(ZonedDateTime date, int years, int monthsOfYear, int dayOfMonth,
            int hoursOfDay, int minutesOfHour, int secondsOfMinute) {
        return date.getSecond() == secondsOfMinute && date.getMinute() == minutesOfHour
                && date.getHour() == hoursOfDay && date.getDayOfMonth() == dayOfMonth
                && date.getMonthValue() == monthsOfYear && date.getYear() == years;
    }

    private static final class ExecutionTimeResult {
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

        @Override
        public String toString() {
            return "ExecutionTimeResult{" + "time=" + time + ", isMatch=" + isMatch + '}';
        }
    }
}
