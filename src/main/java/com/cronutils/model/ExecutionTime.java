package com.cronutils.model;

import com.cronutils.model.field.*;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;

import java.util.*;

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

class ExecutionTime {
    private List<Integer> seconds;
    private List<Integer> minutes;
    private List<Integer> hours;
    private List<Integer> months;
    private List<Integer> years;
    //specific to year and/or month. Should be evaluated after universal values are set.
    private List<Integer> daysOfMonth;
    private List<Integer> daysOfWeek;

    private ExecutionTime(Map<CronFieldName, CronField> fields){
        fields = new HashMap<CronFieldName, CronField>(fields);
        if(fields.get(CronFieldName.SECOND)==null){
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
        if(fields.get(CronFieldName.YEAR)==null){
            fields.put(CronFieldName.YEAR,
                    new CronField(
                            CronFieldName.YEAR,
                            new On(
                                    FieldConstraintsBuilder.instance()
                                            .forField(CronFieldName.YEAR)
                                            .createConstraintsInstance(),
                                    ""+DateTime.now().getYear()
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

    public static ExecutionTime forCron(Cron cron){
        return new ExecutionTime(cron.retrieveFieldsAsMap());
    }

    public DateTime afterDate(DateTime date){
        Set<Integer> seconds = Sets.newHashSet();
        Set<Integer> minutes = Sets.newHashSet();
        Set<Integer> hours = Sets.newHashSet();
        seconds.add(nextValue(this.seconds, date.getSecondOfMinute()));
        seconds.add(this.seconds.get(0));
        if(this.seconds.contains(date.getSecondOfMinute())){
            seconds.add(date.getSecondOfMinute());
        }

        minutes.add(nextValue(this.minutes, date.getMinuteOfHour()));
        minutes.add(this.minutes.get(0));
        if(this.minutes.contains(date.getMinuteOfHour())){
            minutes.add(date.getMinuteOfHour());
        }

        hours.add(nextValue(this.hours, date.getHourOfDay()));
        hours.add(this.hours.get(0));
        if(this.hours.contains(date.getHourOfDay())){
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
        for(int year : years){
            for(int month: months){
                List<Integer> days = Lists.newArrayList();
                final int maxDayMonth = new DateTime(year, month, 1, 12, 0, 0).dayOfMonth().getMaximumValue();
                days.addAll(Collections2.filter(daysOfMonth, new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer integer) {
                        return integer <= maxDayMonth;
                    }
                }));
                DateTime daysDay = new DateTime(year, month, 1, 0, 0);
                for(int j=0; j < maxDayMonth; j++){
                    if(daysOfWeek.contains(daysDay.getDayOfWeek()-1)){
                        days.add(daysDay.getDayOfMonth());
                        daysDay = daysDay.plusDays(1);
                    }
                }
                Collections.sort(days);
                List<Integer> daysSubset = Lists.newArrayList();
                daysSubset.add(nextValue(days, date.getDayOfMonth()));
                daysSubset.add(date.getDayOfMonth());
                daysSubset.add(days.get(0));
                for(int day: daysSubset){
                    for(int hour : hours){
                        for(int minute : minutes){
                            for(int second : seconds){
                                long dist = Long.parseLong(
                                        String.format("%04d%02d%02d%02d%02d%02d",
                                                year, month, day, hour, minute, second
                                        )
                                );
                                long diff = dist-reference;
                                if(leastDistance==-1){
                                    leastDistance = diff;
                                }
                                if(diff < leastDistance){
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

//    public DateTime beforeDate(DateTime date){
//    }


    private int nextValue(List<Integer> values, int reference){
        //TODO improve using binary search
        for(Integer value : values){
            if(value > reference){
                return value;
            }
        }
        return values.get(0);
    }

    private List<Integer> fromFieldToTimeValues(FieldExpression fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
        if(fieldExpression == null){
            values.add(0);
            return values;
        }
        if(fieldExpression instanceof And){
            values = fromFieldToTimeValues((And)fieldExpression, max);
        }
        if(fieldExpression instanceof Between){
            values = fromFieldToTimeValues((Between)fieldExpression, max);
        }
        if(fieldExpression instanceof On){
            values = fromFieldToTimeValues((On)fieldExpression, max);
        }
        if(fieldExpression instanceof Always){
            values = fromFieldToTimeValues((Always)fieldExpression, max);
        }
        Collections.sort(values);
        return values;
    }

    private List<Integer> fromFieldToTimeValues(And fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
        for(FieldExpression expression : fieldExpression.getExpressions()){
            values.addAll(fromFieldToTimeValues(expression, max));
        }
        return values;
    }

    private List<Integer> fromFieldToTimeValues(Between fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
        int every = fieldExpression.getEvery().getTime();
        for(int j = fieldExpression.getFrom(); j < fieldExpression.getTo() + 1; j+=every){
            values.add(j);
        }
        return values;
    }

    private List<Integer> fromFieldToTimeValues(On fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
        values.add(fieldExpression.getTime());
        return values;
    }

    private List<Integer> fromFieldToTimeValues(Always fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
        int every = fieldExpression.getEvery().getTime();
        for(int j = 1; j <= max; j+=every){
            values.add(j);
        }
        return values;
    }

    private int getMaxForCronField(CronFieldName cronFieldName){
        switch (cronFieldName){
            case YEAR:
                return DateTime.now().getYear() + 1;
            case MONTH:
                return 12;
            case DAY_OF_MONTH:
                return 31;
            case DAY_OF_WEEK:
                return 7;
            default:
                return 60;
        }
    }
}
