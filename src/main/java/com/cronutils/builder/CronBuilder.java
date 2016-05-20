package com.cronutils.builder;

import com.cronutils.builder.model.expression.CronField;
import com.cronutils.builder.model.expression.Every;
import com.cronutils.builder.model.expression.FieldExpression;
import com.cronutils.builder.model.expression.On;
import com.cronutils.model.field.CronFieldName;

import com.cronutils.model.field.value.IntegerFieldValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CronBuilder {
    private Map<CronFieldName, List<FieldExpression>> expressions;

    public CronBuilder(){
        this.expressions = Maps.newHashMap();
        for(CronFieldName name : CronFieldName.values()){
            expressions.put(name, Lists.<FieldExpression>newArrayList());
        }
    }

    public static CronBuilder createCronFor(){
        return new CronBuilder();
    }

    public CronBuilder every(CronFieldName name){
        return every(1, name);
    }

    public CronBuilder every(int frequency, CronFieldName name){
        expressions.get(name).add(new Every(new IntegerFieldValue(frequency)));
        return this;
    }

    public CronBuilder on(int onValue, CronFieldName name){
        expressions.get(name).add(new On(new IntegerFieldValue(onValue)));
        return this;
    }

    public CronBuilder on(CronField cronField){
        expressions.get(cronField.getField()).add(cronField.getExpression());
        return this;
    }


    public CronFieldName second(){
        return CronFieldName.SECOND;
    }

    public CronFieldName seconds(){
        return second();
    }

    public CronField monday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(1)));
    }
    public CronField tuesday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(2)));
    }
    public CronField wednesday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(3)));
    }
    public CronField thursday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(4)));
    }
    public CronField friday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(5)));
    }
    public CronField saturday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(6)));
    }
    public CronField sunday(){
        return new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(7)));
    }
}
