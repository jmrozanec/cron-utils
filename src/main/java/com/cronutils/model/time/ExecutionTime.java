package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.field.*;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.*;

import static com.cronutils.model.time.ExecutionTime.Position.*;

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
    private List<Integer> seconds;
    private List<Integer> minutes;
    private List<Integer> hours;
    private List<Integer> months;
    private List<Integer> years;
    //specific to year and/or month. Should be evaluated after universal values are set.
    private List<Integer> daysOfMonth;
    private List<Integer> daysOfWeek;

    /**
     * Constructor
     * @param fields - Map<CronFieldName, CronField> with cron fields;
     */
    @VisibleForTesting
    ExecutionTime(Map<CronFieldName, CronField> fields) {
        fields = new HashMap<CronFieldName, CronField>(fields);
        if (fields.get(CronFieldName.SECOND) == null) {
            fields.put(CronFieldName.SECOND,
                    new CronField(
                            CronFieldName.SECOND,
                            new On(
                                    FieldConstraintsBuilder.instance()
                                            .forField(CronFieldName.SECOND)
                                            .createConstraintsInstance(), "0")
                    )
            );
        }
        if (fields.get(CronFieldName.YEAR) == null) {
            fields.put(CronFieldName.YEAR,
                    new CronField(
                            CronFieldName.YEAR,
                            new On(
                                    FieldConstraintsBuilder.instance()
                                            .forField(CronFieldName.YEAR)
                                            .createConstraintsInstance(),
                                    "" + DateTime.now().getYear()
                            )
                    )
            );
        }
        seconds = fromFieldToTimeValues(
                fields.get(CronFieldName.SECOND).getExpression(),
                getMaxForCronField(CronFieldName.SECOND)
        );
        minutes = fromFieldToTimeValues(
                fields.get(CronFieldName.MINUTE).getExpression(),
                getMaxForCronField(CronFieldName.MINUTE)
        );
        hours = fromFieldToTimeValues(
                fields.get(CronFieldName.HOUR).getExpression(),
                getMaxForCronField(CronFieldName.HOUR)
        );
        daysOfWeek = fromFieldToTimeValues(
                fields.get(CronFieldName.DAY_OF_WEEK).getExpression(),
                getMaxForCronField(CronFieldName.DAY_OF_WEEK)
        );
        daysOfMonth = fromFieldToTimeValues(
                fields.get(CronFieldName.DAY_OF_MONTH).getExpression(),
                getMaxForCronField(CronFieldName.DAY_OF_MONTH)
        );
        months = fromFieldToTimeValues(
                fields.get(CronFieldName.MONTH).getExpression(),
                getMaxForCronField(CronFieldName.MONTH)
        );
        years = fromFieldToTimeValues(
                fields.get(CronFieldName.YEAR).getExpression(),
                getMaxForCronField(CronFieldName.YEAR)
        );
    }

    /**
     * Creates execution time for given Cron
     * @param cron - Cron instance
     * @return ExecutionTime instance
     */
    public static ExecutionTime forCron(Cron cron) {
        return new ExecutionTime(cron.retrieveFieldsAsMap());
    }

    /**
     * Provide nearest date for next execution.
     * @param date - jodatime DateTime instance. If null, a NullPointerException will be raised.
     * @return DateTime instance, never null. Next execution time.
     */
    public DateTime nextExecution(DateTime date) {
        Validate.notNull(date);
        return nearestDateTime(date, NEXT,
                new Predicate<Boolean>(){
                    @Override
                    public boolean apply(Boolean value) {
                        return true;
                    }
                },
                new Predicate<Long>(){
                   @Override
                   public boolean apply(Long value) {
                       return value > 0;
                   }
               }
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
        return nearestDateTime(date, PREVIOUS,
                new Predicate<Boolean>(){
                    @Override
                    public boolean apply(Boolean value) {
                        return false;
                    }
                },
                new Predicate<Long>(){
                    @Override
                    public boolean apply(Long value) {
                        return value < 0;
                    }
                }
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

    /**
     * Parses field expressions and generates a list of all matching values considering max
     * as upper bound to match the criteria.
     * @param fieldExpression - FieldExpression instance. If null, a zero is issued.
     * @param max - max accepted value.
     * @return List<Integer> - list of matching integers
     */
    @VisibleForTesting
    List<Integer> fromFieldToTimeValues(FieldExpression fieldExpression, int max) {
        List<Integer> values = Lists.newArrayList();
        if (fieldExpression == null) {
            values.add(0);
            return values;
        }
        if (fieldExpression instanceof And) {
            values = fromFieldToTimeValues((And) fieldExpression, max);
        }
        if (fieldExpression instanceof Between) {
            values = fromFieldToTimeValues((Between) fieldExpression, max);
        }
        if (fieldExpression instanceof On) {
            values = fromFieldToTimeValues((On) fieldExpression, max);
        }
        if (fieldExpression instanceof Always) {
            values = fromFieldToTimeValues((Always) fieldExpression, max);
        }
        Collections.sort(values);
        return values;
    }

    /**
     * Generates a list of all matching values considering max
     * @param fieldExpression - And instance. If null, a NullPointerException will be raised.
     * @param max - max accepted value.
     * @return List<Integer> - list of matching integers
     */
    @VisibleForTesting
    List<Integer> fromFieldToTimeValues(And fieldExpression, int max) {
        Validate.notNull(fieldExpression);
        List<Integer> values = Lists.newArrayList();
        for (FieldExpression expression : fieldExpression.getExpressions()) {
            values.addAll(fromFieldToTimeValues(expression, max));
        }
        return values;
    }

    /**
     * Generates a list of all matching values considering max
     * @param fieldExpression - Between instance. If null, a NullPointerException will be raised.
     * @param max - max accepted value.
     * @return List<Integer> - list of matching integers
     */
    @VisibleForTesting
    List<Integer> fromFieldToTimeValues(Between fieldExpression, int max) {
        Validate.notNull(fieldExpression);
        List<Integer> values = Lists.newArrayList();
        int every = fieldExpression.getEvery().getTime();
        for (int j = fieldExpression.getFrom(); j < fieldExpression.getTo() + 1; j += every) {
            values.add(j);
        }
        return values;
    }

    /**
     * Generates a list of all matching values considering max
     * @param fieldExpression - On instance. If null, a NullPointerException will be raised.
     * @param max - max accepted value.
     * @return List<Integer> - list of matching integers
     */
    @VisibleForTesting
    List<Integer> fromFieldToTimeValues(On fieldExpression, int max) {
        Validate.notNull(fieldExpression);
        List<Integer> values = Lists.newArrayList();
        values.add(fieldExpression.getTime());
        return values;
    }

    /**
     * Generates a list of all matching values considering max
     * @param fieldExpression - Always instance. If null, a NullPointerException will be raised.
     * @param max - max accepted value.
     * @return List<Integer> - list of matching integers
     */
    @VisibleForTesting
    List<Integer> fromFieldToTimeValues(Always fieldExpression, int max) {
        Validate.notNull(fieldExpression);
        List<Integer> values = Lists.newArrayList();
        int every = fieldExpression.getEvery().getTime();
        for (int j = 1; j <= max; j += every) {
            values.add(j);
        }
        return values;
    }

    /**
     * Returns max value for given cron field name;
     * @param cronFieldName - CronFieldName
     * @return int - max value
     */
    @VisibleForTesting
    int getMaxForCronField(CronFieldName cronFieldName) {
        switch (cronFieldName) {
            case YEAR:
                return DateTime.now().getYear() + 1;//TODO should be contextual to the date they ask for
            case MONTH:
                return 12;
            case DAY_OF_MONTH:
                return 31;
            case DAY_OF_WEEK:
                return 7;
            case HOUR:
                return 24;
            default:
                return 60;
        }
    }

    /**
     * Implements binary search looking for closest value to the given one
     * based on desired value (previous or next)
     * @param list - list of values
     * @param key - reference parameter
     * @param desiredValue - desired value: next or previous to key
     * @return desired value int
     */
    private int binarySearch(List<Integer> list, int key, Position desiredValue) {
        int lowerbound = 0;
        int upperbound = list.size();
        int position;
        position = (lowerbound + upperbound) / 2;

        while ((list.get(position) != key) && (lowerbound <= upperbound)) {
            if (list.get(position) > key) {
                upperbound = position - 1;
            } else {
                lowerbound = position + 1;
            }
            position = (lowerbound + upperbound) / 2;
        }
        if (lowerbound <= upperbound) {
            switch (desiredValue) {
                case NEXT:
                    return list.get(position + 1);
                case PREVIOUS:
                    return list.get(position - 1);
            }
        } else {
            switch (desiredValue) {
                case PREVIOUS:
                    return list.get(upperbound);
                case NEXT:
                    return list.get(lowerbound);
            }
        }
        return 0;
    }

    enum Position {
        NEXT, PREVIOUS
    }

    /**
     * Calculates nearest date.
     * @param date
     * @param position
     * @param leastDistancePredicate
     * @param diffZeroPredicate
     * @return
     */
    private DateTime nearestDateTime(DateTime date, Position position,
                                     Predicate<Boolean> leastDistancePredicate,
                                     Predicate<Long> diffZeroPredicate){
        Set<Integer> seconds = Sets.newHashSet();
        Set<Integer> minutes = Sets.newHashSet();
        Set<Integer> hours = Sets.newHashSet();
        seconds.add(binarySearch(this.seconds, date.getSecondOfMinute(), position));
        seconds.add(this.seconds.get(0));
        if (this.seconds.contains(date.getSecondOfMinute())) {
            seconds.add(date.getSecondOfMinute());
        }

        minutes.add(binarySearch(this.minutes, date.getMinuteOfHour(), position));
        minutes.add(this.minutes.get(0));
        if (this.minutes.contains(date.getMinuteOfHour())) {
            minutes.add(date.getMinuteOfHour());
        }

        hours.add(binarySearch(this.hours, date.getHourOfDay(), position));
        hours.add(this.hours.get(0));
        if (this.hours.contains(date.getHourOfDay())) {
            hours.add(date.getHourOfDay());
        }

        long reference = Long.parseLong(
                String.format("%04d%02d%02d%02d%02d%02d",
                        date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
                        date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute()
                )
        );

        DateTime nearestDate = null;
        long leastDistance = -1;
        for (int year : years) {
            for (int month : months) {
                List<Integer> days = Lists.newArrayList();
                final int maxDayMonth = new DateTime(year, month, 1, 12, 0, 0).dayOfMonth().getMaximumValue();
                days.addAll(Collections2.filter(daysOfMonth, new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer integer) {
                        return integer <= maxDayMonth;
                    }
                }));
                DateTime daysDay = new DateTime(year, month, 1, 0, 0);
                for (int j = 0; j < maxDayMonth; j++) {
                    if (daysOfWeek.contains(daysDay.getDayOfWeek() - 1)) {
                        days.add(daysDay.getDayOfMonth());
                        daysDay = daysDay.plusDays(1);
                    }
                }
                Collections.sort(days);
                List<Integer> daysSubset = Lists.newArrayList();
                daysSubset.add(binarySearch(days, date.getDayOfMonth(), position));
                daysSubset.add(date.getDayOfMonth());
                daysSubset.add(days.get(0));
                for (int day : daysSubset) {
                    for (int hour : hours) {
                        for (int minute : minutes) {
                            for (int second : seconds) {
                                long dist = Long.parseLong(
                                        String.format("%04d%02d%02d%02d%02d%02d",
                                                year, month, day, hour, minute, second
                                        )
                                );
                                long diff = dist - reference;
                                if (leastDistance == -1) {
                                    leastDistance = diff;
                                }
                                if (leastDistancePredicate.apply(diff < leastDistance) && diffZeroPredicate.apply(diff)) {
                                    nearestDate = new DateTime(year, month, day, hour, minute, second);
                                }
                            }
                        }
                    }
                }
            }
        }
        return nearestDate;
    }
}
