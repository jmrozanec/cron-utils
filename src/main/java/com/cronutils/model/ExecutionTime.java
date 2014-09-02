package com.cronutils.model;

import com.cronutils.model.field.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

class ExecutionTime {

    private Map<CronFieldName, List<Integer>> executionTimes;

    private ExecutionTime(Map<CronFieldName, CronField> fields){
        this.executionTimes = Maps.newHashMap();
    }

    public static ExecutionTime forCron(Cron cron){
        return new ExecutionTime(cron.retrieveFieldsAsMap());
    }

//    public DateTime afterDate(DateTime date){
//    }
//
//    public DateTime beforeDate(DateTime date){
//    }

    private List<Integer> fromFieldToTimeValues(FieldExpression fieldExpression, int max){
        if(fieldExpression instanceof And){
            return fromFieldToTimeValues((And)fieldExpression, max);
        }
        if(fieldExpression instanceof Between){
            return fromFieldToTimeValues((Between)fieldExpression, max);
        }
        if(fieldExpression instanceof On){
            return fromFieldToTimeValues((On)fieldExpression, max);
        }
        if(fieldExpression instanceof Always){
            return fromFieldToTimeValues((Always)fieldExpression, max);
        }
        return Lists.newArrayList();
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
}
