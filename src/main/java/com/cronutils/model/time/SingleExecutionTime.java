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

package com.cronutils.model.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;

import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
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
import static com.cronutils.model.time.generator.FieldValueGeneratorFactory.*;
import static com.cronutils.utils.Predicates.not;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Calculates execution time given a cron pattern.
 */
public class SingleExecutionTime implements ExecutionTime {

    private static final int MAX_ITERATIONS = 100_000;

    private static final LocalTime MAX_SECONDS = LocalTime.MAX.truncatedTo(SECONDS);

    private final CronDefinition cronDefinition;
    private final FieldValueGenerator yearsValueGenerator;
    private final CronField daysOfWeekCronField;
    private final CronField daysOfMonthCronField;
    private final CronField daysOfYearCronField;

    private final TimeNode months;
    private final TimeNode hours;
    private final TimeNode minutes;
    private final TimeNode seconds;

    @VisibleForTesting
    SingleExecutionTime(final CronDefinition cronDefinition, final CronField yearsValueCronField, final CronField daysOfWeekCronField,
            final CronField daysOfMonthCronField, final CronField daysOfYearCronField, final TimeNode months, final TimeNode hours,
            final TimeNode minutes, final TimeNode seconds) {
        this.cronDefinition = Preconditions.checkNotNull(cronDefinition);
        FieldValueGenerator alwaysGenerator = FieldValueGeneratorFactory.forCronField(new CronField(CronFieldName.YEAR, Always.always(), FieldConstraintsBuilder.instance().createConstraintsInstance()));
        if(cronDefinition.containsFieldDefinition(CronFieldName.YEAR)){
            if(!cronDefinition.getFieldDefinition(CronFieldName.YEAR).isOptional()){
                Preconditions.checkNotNull(yearsValueCronField);
            }
            this.yearsValueGenerator = yearsValueCronField==null?alwaysGenerator:createYearValueGeneratorInstance(yearsValueCronField);
        } else{
            this.yearsValueGenerator = alwaysGenerator;
        }
        this.daysOfWeekCronField = Preconditions.checkNotNull(daysOfWeekCronField);
        this.daysOfMonthCronField = Preconditions.checkNotNull(daysOfMonthCronField);
        this.daysOfYearCronField = daysOfYearCronField;
        this.months = Preconditions.checkNotNull(months);
        this.hours = Preconditions.checkNotNull(hours);
        this.minutes = Preconditions.checkNotNull(minutes);
        this.seconds = Preconditions.checkNotNull(seconds);
    }

    /**
     * Provide nearest date for next execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Contains next execution time or empty.
     */
    public Optional<ZonedDateTime> nextExecution(final ZonedDateTime date) {
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime nextMatch = nextClosestMatch(date);
            if (nextMatch.equals(date)) {
                nextMatch = nextClosestMatch(date.plusSeconds(1));
            }
            return Optional.of(nextMatch);
        } catch (final NoSuchValueException e) {
            return Optional.empty();
        }
    }

    /**
     * If date is not match, will return next closest match.
     * If date is match, will return this date.
     *
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException if there is no potential next year
     */
    private ZonedDateTime nextClosestMatch(final ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult result = new ExecutionTimeResult(date, false);
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            result = potentialNextClosestMatch(result.getTime());
            if (result.isMatch()) {
                return result.getTime();
            }
            if (result.getTime().getYear() - date.getYear() > 100) {
                throw new NoSuchValueException();
            }
        }

        throw new NoSuchValueException();
    }

    private ExecutionTimeResult potentialNextClosestMatch(final ZonedDateTime date) throws NoSuchValueException {
        final List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear()).stream().filter(d->d>=date.getYear()).collect(Collectors.toList());
        final int lowestMonth = months.getValues().get(0);
        final int lowestHour = hours.getValues().get(0);
        final int lowestMinute = minutes.getValues().get(0);
        final int lowestSecond = seconds.getValues().get(0);

        if (year.isEmpty()) {
            return getNextPotentialYear(date, lowestMonth, lowestHour, lowestMinute, lowestSecond);
        }

        if (!months.getValues().contains(date.getMonthValue())) {
            return getNextPotentialMonth(date, lowestHour, lowestMinute, lowestSecond);
        }

        final Optional<TimeNode> optionalDays = generateDays(cronDefinition, date);
        if (!optionalDays.isPresent()) {
            return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
        }
        final TimeNode node = optionalDays.get();

        if (!node.getValues().contains(date.getDayOfMonth())) {
            return getNextPotentialDayOfMonth(date, lowestHour, lowestMinute, lowestSecond, node);
        }
        if (!hours.getValues().contains(date.getHour())) {
            return getNextPotentialHour(date);
        }
        if (!minutes.getValues().contains(date.getMinute())) {
            return getNextPotentialMinute(date);
        }
        if (!seconds.getValues().contains(date.getSecond())) {
            return getNextPotentialSecond(date);
        }

        return new ExecutionTimeResult(date, true);
    }

    private ExecutionTimeResult getNextPotentialYear(final ZonedDateTime date,
                                                     final int lowestMonth,
                                                     final int lowestHour,
                                                     final int lowestMinute,
                                                     final int lowestSecond)
                                                     throws NoSuchValueException {
        final int newYear = yearsValueGenerator.generateNextValue(date.getYear());
        final Optional<TimeNode> optionalDays = generateDays(cronDefinition, ZonedDateTime.of(
                LocalDate.of(newYear, lowestMonth, 1),
                LocalTime.MIN,
                date.getZone())
        );
        if (optionalDays.isPresent()) {
            final List<Integer> days = optionalDays.get().getValues();
            return new ExecutionTimeResult(ZonedDateTime.of(
                    LocalDate.of(newYear, lowestMonth, days.get(0)),
                    LocalTime.of(lowestHour, lowestMinute, lowestSecond), date.getZone()), false);
        } else {
            return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
        }
    }

    private ExecutionTimeResult getNextPotentialMonth(final ZonedDateTime date, final int lowestHour, final int lowestMinute, final int lowestSecond) {
        NearestValue nearestValue;
        nearestValue = months.getNextValue(date.getMonthValue(), 0);
        final int nextMonths = nearestValue.getValue();
        if (nearestValue.getShifts() > 0) {
            return new ExecutionTimeResult(date.truncatedTo(DAYS).withMonth(1).withDayOfMonth(1).plusYears(nearestValue.getShifts()), false);
        }
        final Optional<TimeNode> optionalDays = generateDays(cronDefinition,
                ZonedDateTime.of(LocalDateTime.of(date.getYear(), nextMonths, 1, 0, 0), date.getZone()));
        if (optionalDays.isPresent()) {
            final List<Integer> days = optionalDays.get().getValues();
            return new ExecutionTimeResult(
                    date.truncatedTo(SECONDS).withMonth(nextMonths).withDayOfMonth(days.get(0))
                            .with(LocalTime.of(lowestHour, lowestMinute, lowestSecond)), false);
        } else {
            return new ExecutionTimeResult(toBeginOfNextMonth(date), false);
        }
    }

    private ExecutionTimeResult getNextPotentialDayOfMonth(final ZonedDateTime date,
                                                           final int lowestHour,
                                                           final int lowestMinute,
                                                           final int lowestSecond,
                                                           final TimeNode node) {
        final NearestValue nearestValue = node.getNextValue(date.getDayOfMonth(), 0);
        if (nearestValue.getShifts() > 0) {
            return new ExecutionTimeResult(date.truncatedTo(DAYS).withDayOfMonth(1).plusMonths(nearestValue.getShifts()), false);
        }
        return new ExecutionTimeResult(date.truncatedTo(SECONDS).withDayOfMonth(nearestValue.getValue())
                .with(LocalTime.of(lowestHour, lowestMinute, lowestSecond)), false);
    }

    private ExecutionTimeResult getNextPotentialHour(final ZonedDateTime date) throws NoSuchValueException {
        return getNextPotentialValue(date, hours, ChronoField.HOUR_OF_DAY);
    }

    private ExecutionTimeResult getNextPotentialMinute(final ZonedDateTime date) throws NoSuchValueException {
        return getNextPotentialValue(date, minutes, ChronoField.MINUTE_OF_HOUR);
    }

    private ExecutionTimeResult getNextPotentialSecond(final ZonedDateTime date) throws NoSuchValueException {
        return getNextPotentialValue(date, seconds, ChronoField.SECOND_OF_MINUTE);
    }

    private static ExecutionTimeResult getNextPotentialValue(
            final ZonedDateTime date,
            final TimeNode node,
            final TemporalField field) throws NoSuchValueException {
        Set<Integer> values = new HashSet<>(node.values);

        TemporalUnit unit = field.getBaseUnit();

        long maximum = field.range().getMaximum();
        long minimum = field.range().getMinimum();
        long range = maximum - minimum;

        ZonedDateTime newDate = date;
        for (long i = 0; i < 2 * range; i++) {
            newDate = newDate.plus(1, unit);

            if (values.contains(newDate.get(field))) {
                newDate = newDate
                        .truncatedTo(unit);
                return new ExecutionTimeResult(newDate, false);
            }
        }

        throw new NoSuchValueException();
    }

    private ZonedDateTime toBeginOfNextMonth(final ZonedDateTime datetime) {
        return datetime.truncatedTo(DAYS).plusMonths(1).withDayOfMonth(1);
    }

    /**
     * If date is not match, will return previous closest match.
     * If date is match, will return this date.
     *
     * @param date - reference ZonedDateTime instance - never null;
     * @return ZonedDateTime instance, never null. Value obeys logic specified above.
     * @throws NoSuchValueException if there is not previous year
     */
    private ZonedDateTime previousClosestMatch(final ZonedDateTime date) throws NoSuchValueException {
        ExecutionTimeResult result = new ExecutionTimeResult(date, false);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            result = potentialPreviousClosestMatch(result.getTime());
            if (result.isMatch()) {
                return result.getTime();
            }
        }
        throw new NoSuchValueException();
    }

    private ExecutionTimeResult potentialPreviousClosestMatch(final ZonedDateTime date) throws NoSuchValueException {
        //int startyear = cronDefinition.getFieldDefinition(CronFieldName.YEAR).getConstraints().getStartRange();
        //final List<Integer> year = yearsValueGenerator.generateCandidates(startyear, date.getYear());
        final List<Integer> year = yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
        final Optional<TimeNode> optionalDays = generateDays(cronDefinition, date);
        TimeNode days;
        if (optionalDays.isPresent()) {
            days = optionalDays.get();
        } else {
            return new ExecutionTimeResult(toEndOfPreviousMonth(date), false);
        }
        final int highestMonth = months.getValues().get(months.getValues().size() - 1);
        final int highestDay = days.getValues().get(days.getValues().size() - 1);
        final int highestHour = hours.getValues().get(hours.getValues().size() - 1);
        final int highestMinute = minutes.getValues().get(minutes.getValues().size() - 1);
        final int highestSecond = seconds.getValues().get(seconds.getValues().size() - 1);

        if (year.isEmpty()) {
            return getPreviousPotentialYear(date, days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
        }
        //TODO Issue 305
        /*
        else{
            if(!year.contains(date.getYear())){
                Optional<Integer> validprevyear = year.stream().filter(y->y<date.getYear()).max(Integer::compareTo);
                if(validprevyear.isPresent()){
                    int minus = date.getYear()-validprevyear.get();
                    return getPreviousPotentialYear(date.minusYears(minus), days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
                }else{
                    return getPreviousPotentialYear(date.minusYears(1), days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
                }
            }
        }
        */
        if (!months.getValues().contains(date.getMonthValue())) {
            return getPreviousPotentialMonth(date, highestDay, highestHour, highestMinute, highestSecond);
        }
        if (!days.getValues().contains(date.getDayOfMonth())) {
            return getPreviousPotentialDayOfMonth(date, days, highestHour, highestMinute, highestSecond);
        }
        if (!hours.getValues().contains(date.getHour())) {
            return getPreviousPotentialHour(date);
        }
        if (!minutes.getValues().contains(date.getMinute())) {
            return getPreviousPotentialMinute(date);
        }
        if (!seconds.getValues().contains(date.getSecond())) {
            return getPreviousPotentialSecond(date);
        }
        return new ExecutionTimeResult(date.truncatedTo(SECONDS), true);
    }

    private ExecutionTimeResult getPreviousPotentialYear(final ZonedDateTime date, final TimeNode days, final int highestMonth, int highestDay,
            final int highestHour, final int highestMinute, final int highestSecond) throws NoSuchValueException {
        NearestValue nearestValue;
        ZonedDateTime newDate;
        final int previousYear = yearsValueGenerator.generatePreviousValue(date.getYear());
        if (highestDay > 28) {
            final int highestDayOfMonth = LocalDate.of(previousYear, highestMonth, 1).lengthOfMonth();
            if (highestDay > highestDayOfMonth) {
                nearestValue = days.getPreviousValue(highestDay, 1);
                if (nearestValue.getShifts() > 0) {
                    newDate = ZonedDateTime.of(
                            LocalDate.of(previousYear, highestMonth, 1),
                            MAX_SECONDS,
                            date.getZone()
                    ).minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
                    return new ExecutionTimeResult(newDate, false);
                } else {
                    highestDay = nearestValue.getValue();
                }
            }
        }
        return new ExecutionTimeResult(ZonedDateTime.of(
                LocalDate.of(previousYear, highestMonth, highestDay),
                LocalTime.of(highestHour, highestMinute, highestSecond),
                date.getZone()),
                false);
    }

    private ExecutionTimeResult getPreviousPotentialMonth(final ZonedDateTime date, final int highestDay, final int highestHour,
            final int highestMinute, final int highestSecond) {
        NearestValue nearestValue;
        ZonedDateTime newDate;
        nearestValue = months.getPreviousValue(date.getMonthValue(), 0);
        final int previousMonths = nearestValue.getValue();
        if (nearestValue.getShifts() > 0) {
            newDate = ZonedDateTime.of(
                    LocalDate.of(date.getYear(), 12, 31),
                    MAX_SECONDS,
                    date.getZone()
            ).minusYears(nearestValue.getShifts());
            return new ExecutionTimeResult(newDate, false);
        }
        else {
            newDate = ZonedDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0, 0, date.getZone()).minusNanos(1);

            return new ExecutionTimeResult(newDate, false);
        }
    }

    private ExecutionTimeResult getPreviousPotentialDayOfMonth(final ZonedDateTime date, final TimeNode days, final int highestHour, final int highestMinute,
            final int highestSecond) {
        NearestValue nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
        if (nearestValue.getShifts() > 0) {
            ZonedDateTime newDate = ZonedDateTime.of(
                    LocalDate.of(date.getYear(), date.getMonthValue(), 1),
                    MAX_SECONDS,
                    date.getZone()
            ).minusMonths(nearestValue.getShifts()).with(lastDayOfMonth());
            return new ExecutionTimeResult(newDate, false);
        }
        return new ExecutionTimeResult(date.withDayOfMonth(nearestValue.getValue())
                .with(LocalTime.of(highestHour, highestMinute, highestSecond)).truncatedTo(SECONDS), false);
    }

    private ExecutionTimeResult getPreviousPotentialHour(final ZonedDateTime date) throws NoSuchValueException {
        return getPreviousPotentialValue(date, hours, ChronoField.HOUR_OF_DAY);
    }

    private ExecutionTimeResult getPreviousPotentialMinute(final ZonedDateTime date) throws NoSuchValueException {
        return getPreviousPotentialValue(date, minutes, ChronoField.MINUTE_OF_HOUR);
    }

    private ExecutionTimeResult getPreviousPotentialSecond(final ZonedDateTime date) throws NoSuchValueException {
        return getPreviousPotentialValue(date, seconds, ChronoField.SECOND_OF_MINUTE);
    }

    private static ExecutionTimeResult getPreviousPotentialValue(
            final ZonedDateTime date,
            final TimeNode node,
            final TemporalField field) throws NoSuchValueException {
        Set<Integer> values = new HashSet<>(node.values);

        TemporalUnit unit = field.getBaseUnit();

        long maximum = field.range().getMaximum();
        long minimum = field.range().getMinimum();
        long range = maximum - minimum;

        ZonedDateTime newDate = date;
        for (long i = 0; i < 2 * range; i++) {
            newDate = newDate.minus(1, unit);

            if (values.contains(newDate.get(field))) {
                newDate = newDate
                        .truncatedTo(unit)
                        .plus(1, unit)
                        .minusSeconds(1);
                return new ExecutionTimeResult(newDate, false);
            }
        }

        throw new NoSuchValueException();
    }

    private ZonedDateTime toEndOfPreviousMonth(final ZonedDateTime datetime) {
        final ZonedDateTime previousMonth = datetime.minusMonths(1).with(lastDayOfMonth());
        final int highestHour = hours.getValues().get(hours.getValues().size() - 1);
        final int highestMinute = minutes.getValues().get(minutes.getValues().size() - 1);
        final int highestSecond = seconds.getValues().get(seconds.getValues().size() - 1);
        return ZonedDateTime
                .of(previousMonth.getYear(), previousMonth.getMonth().getValue(), previousMonth.getDayOfMonth(), highestHour, highestMinute, highestSecond, 0,
                        previousMonth.getZone());
    }

    private Optional<TimeNode> generateDays(final CronDefinition cronDefinition, final ZonedDateTime date) {
        if (isGenerateDaysAsDoY(cronDefinition)) {
            return generateDayCandidatesUsingDoY(date);
        }
        //If DoW is not supported in custom definition, we just return an empty list.
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) != null && cronDefinition.getFieldDefinition(DAY_OF_MONTH) != null) {
            return generateDaysDoWAndDoMSupported(cronDefinition, date);
        }
        if (cronDefinition.getFieldDefinition(DAY_OF_WEEK) == null) {
            return Optional.of(generateDayCandidatesUsingDoM(date));
        }
        return Optional
                .of(generateDayCandidatesUsingDoW(date, ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()));
    }

    private boolean isGenerateDaysAsDoY(final CronDefinition cronDefinition) {
        if (!cronDefinition.containsFieldDefinition(DAY_OF_YEAR)) {
            return false;
        }

        if (!cronDefinition.getFieldDefinition(DAY_OF_YEAR).getConstraints().getSpecialChars().contains(QUESTION_MARK)) {
            return true;
        }

        return !(daysOfYearCronField.getExpression() instanceof QuestionMark);
    }

    private Optional<TimeNode> generateDayCandidatesUsingDoY(final ZonedDateTime reference) {
        final int year = reference.getYear();
        final int month = reference.getMonthValue();
        final LocalDate date = LocalDate.of(year, 1, 1);
        final int lengthOfYear = date.lengthOfYear();

        final List<Integer> candidates = createDayOfYearValueGeneratorInstance(daysOfYearCronField, year).generateCandidates(1, lengthOfYear);

        final int low = LocalDate.of(year, month, 1).getDayOfYear();
        final int high = month == 12
                ? LocalDate.of(year, 12, 31).getDayOfYear() + 1
                : LocalDate.of(year, month + 1, 1).getDayOfYear();

        final List<Integer> collectedCandidates = candidates.stream().filter(dayOfYear -> dayOfYear >= low && dayOfYear < high)
                .map(dayOfYear -> LocalDate.ofYearDay(reference.getYear(), dayOfYear).getDayOfMonth())
                .collect(Collectors.toList());

        return Optional.of(collectedCandidates).filter(not(List::isEmpty)).map(TimeNode::new);
    }

    private Optional<TimeNode> generateDaysDoWAndDoMSupported(final CronDefinition cronDefinition, final ZonedDateTime date) {
        final boolean questionMarkSupported = cronDefinition.getFieldDefinition(DAY_OF_WEEK).getConstraints().getSpecialChars().contains(QUESTION_MARK);
        if (questionMarkSupported) {
            final List<Integer> candidates = generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(
                    date.getYear(),
                    date.getMonthValue(),
                    ((DayOfWeekFieldDefinition) cronDefinition.getFieldDefinition(DAY_OF_WEEK)).getMondayDoWValue()
            );
            return Optional.of(candidates).filter(not(List::isEmpty)).map(TimeNode::new);
        } else {
            final List<Integer> candidates = generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(
                    date.getYear(), date.getMonthValue(),
                    ((DayOfWeekFieldDefinition)
                            cronDefinition.getFieldDefinition(DAY_OF_WEEK)
                    ).getMondayDoWValue()
            );
            return Optional.of(candidates).filter(not(List::isEmpty)).map(TimeNode::new);
        }
    }

    /**
     * Provide nearest time for next execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time to next execution.
     */
    public Optional<Duration> timeToNextExecution(final ZonedDateTime date) {
        final Optional<ZonedDateTime> next = nextExecution(date);
        return next.map(zonedDateTime -> Duration.between(date, zonedDateTime));
    }

    /**
     * Provide nearest date for last execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Last execution time or empty.
     */
    public Optional<ZonedDateTime> lastExecution(final ZonedDateTime date) {
        Preconditions.checkNotNull(date);
        try {
            ZonedDateTime previousMatch = previousClosestMatch(date);
            if (previousMatch.equals(date)) {
                previousMatch = previousClosestMatch(date.minusSeconds(1));
            }
            return Optional.of(previousMatch);
        } catch (final NoSuchValueException e) {
            return Optional.empty();
        }
    }

    /**
     * Provide nearest time from last execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time from last execution.
     */
    public Optional<Duration> timeFromLastExecution(final ZonedDateTime date) {
        return lastExecution(date).map(zonedDateTime -> Duration.between(zonedDateTime, date));
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
        final boolean isSecondGranularity = cronDefinition.containsFieldDefinition(SECOND);
        if (isSecondGranularity) {
            date = date.truncatedTo(SECONDS);
        } else {
            date = date.truncatedTo(ChronoUnit.MINUTES);
        }

        final Optional<ZonedDateTime> last = lastExecution(date);
        if (last.isPresent()) {
            final Optional<ZonedDateTime> next = nextExecution(last.get());
            if (next.isPresent()) {
                return next.get().equals(date);
            } else {
                boolean everythingInRange = false;
                try {
                    everythingInRange = dateValuesInExpectedRanges(nextClosestMatch(date), date);
                } catch (final NoSuchValueException ignored) {
                    // Why is this ignored?
                }
                try {
                    everythingInRange = dateValuesInExpectedRanges(previousClosestMatch(date), date);
                } catch (final NoSuchValueException ignored) {
                    // Why is this ignored?
                }
                return everythingInRange;
            }
        }
        return false;
    }

    private boolean dateValuesInExpectedRanges(final ZonedDateTime validCronDate, final ZonedDateTime date) {
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

    private List<Integer> generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(final int year, final int month, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(year, month, 1);
        final int lengthOfMonth = date.lengthOfMonth();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfMonthCronField.getExpression() instanceof Always) {
            return createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else {
            final List<Integer> dayOfWeekCandidates = createDayOfWeekValueGeneratorInstance(daysOfWeekCronField,
                    year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth);
            final List<Integer> dayOfMonthCandidates = createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth);
            if (cronDefinition.isMatchDayOfWeekAndDayOfMonth()) {
                final Set<Integer> intersection = new HashSet<>(dayOfWeekCandidates);
                return dayOfMonthCandidates
                        .stream().filter(intersection::contains)
                        .distinct().sorted()
                        .collect(Collectors.toList());
            } else {
                return Stream.concat(dayOfWeekCandidates.stream(), dayOfMonthCandidates.stream())
                        .distinct().sorted()
                        .collect(Collectors.toList());
            }
        }
    }

    private List<Integer> generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(final int year, final int month, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(year, month, 1);
        final int lengthOfMonth = date.lengthOfMonth();
        if (daysOfMonthCronField.getExpression() instanceof Always && daysOfWeekCronField.getExpression() instanceof Always) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfMonthCronField.getExpression() instanceof QuestionMark) {
            return createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else if (daysOfWeekCronField.getExpression() instanceof QuestionMark) {
            return createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month)
                    .generateCandidates(1, lengthOfMonth)
                    .stream().distinct().sorted()
                    .collect(Collectors.toList());
        } else {
            return Stream.concat(
                    createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, year, month, mondayDoWValue).generateCandidates(1, lengthOfMonth).stream(),
                    createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, year, month).generateCandidates(1, lengthOfMonth).stream()
            ).distinct().sorted().collect(Collectors.toList());
        }
    }

    private TimeNode generateDayCandidatesUsingDoM(final ZonedDateTime reference) {
        final LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        final int lengthOfMonth = date.lengthOfMonth();
        final List<Integer> candidates = createDayOfMonthValueGeneratorInstance(daysOfMonthCronField, reference.getYear(), reference.getMonthValue())
                .generateCandidates(1, lengthOfMonth)
                .stream().distinct().sorted()
                .collect(Collectors.toList());
        return new TimeNode(candidates);
    }

    private TimeNode generateDayCandidatesUsingDoW(final ZonedDateTime reference, final WeekDay mondayDoWValue) {
        final LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
        final int lengthOfMonth = date.lengthOfMonth();
        final List<Integer> candidates = createDayOfWeekValueGeneratorInstance(daysOfWeekCronField, reference.getYear(), reference.getMonthValue(), mondayDoWValue)
                .generateCandidates(1, lengthOfMonth)
                .stream().distinct().sorted()
                .collect(Collectors.toList());
        return new TimeNode(candidates);
    }

    private static final class ExecutionTimeResult {
        private final ZonedDateTime time;
        private final boolean isMatch;

        private ExecutionTimeResult(final ZonedDateTime time, final boolean isMatch) {
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
