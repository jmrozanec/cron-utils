package com.cronutils.model;

import com.cronutils.model.field.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Approach 1: [Discarded] brute force, iterate over all possible future dates until finding first matching cron.
//Approach 2: [...] precalculate possible values for each field and take nearest ones
//Approach 3: [...] nearestValue(FieldExpression, int timeValue): return plus -> method to retrieve value for field an sum
//Aproach 4: similar to previous one, but holding data that would contain possible values in structure and set them to date
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
        years = fromFieldToTimeValues(
                        fields.get(CronFieldName.YEAR).getExpression(),
                        getMaxForCronField(CronFieldName.YEAR)
        );
    }

    public static ExecutionTime forCron(Cron cron){
        return new ExecutionTime(cron.retrieveFieldsAsMap());
    }

    public DateTime afterDate(DateTime date){
        MutableDateTime mutableDateTime = date.toMutableDateTime();
        Set<Integer> seconds = Sets.newHashSet();
        Set<Integer> minutes = Sets.newHashSet();
        Set<Integer> hours = Sets.newHashSet();
        seconds.add(nextValue(this.seconds, date.getSecondOfDay()));
        minutes.add(nextValue(this.minutes, date.getMinuteOfDay()));
        hours.add(nextValue(this.hours, date.getHourOfDay()));
        mutableDateTime.setSecondOfDay();
        mutableDateTime.setMinuteOfDay(nextValue(minutes, date.getMinuteOfDay()));
    }

//    public DateTime beforeDate(DateTime date){
//    }

    private int nextValue(List<Integer> values, int reference){
        for(Integer value : values){
            if(value > reference){
                return value;
            }
        }
        return values.get(0);
    }

    private List<Integer> fromFieldToTimeValues(FieldExpression fieldExpression, int max){
        List<Integer> values = Lists.newArrayList();
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
                return DateTime.now().getYear() + 60;
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
