package com.cronutils.builder;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import com.cronutils.model.field.value.SpecialChar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

public class CronBuilder {
    private CronDefinition definition;
    private Map<CronFieldName, CronField> fields;

    private CronBuilder(CronDefinition definition){
        this.definition = definition;
        this.fields = Maps.newHashMap();
    }

    public static CronBuilder cron(CronDefinition definition){
        return new CronBuilder(definition);
    }

    public CronBuilder withYear(FieldExpression expression){
        return addField(CronFieldName.YEAR, expression);
    }

    public CronBuilder withDoM(FieldExpression expression){
        return addField(CronFieldName.DAY_OF_MONTH, expression);
    }

    public CronBuilder withMonth(FieldExpression expression){
        return addField(CronFieldName.MONTH, expression);
    }

    public CronBuilder withDoW(FieldExpression expression){
        return addField(CronFieldName.DAY_OF_WEEK, expression);
    }

    public CronBuilder withHour(FieldExpression expression){
        return addField(CronFieldName.HOUR, expression);
    }

    public CronBuilder withMinute(FieldExpression expression){
        return addField(CronFieldName.MINUTE, expression);
    }

    public CronBuilder withSecond(FieldExpression expression){
        return addField(CronFieldName.SECOND, expression);
    }

    public Cron instance(){
        return new Cron(definition, Lists.newArrayList(fields.values())).validate();
    }

    @VisibleForTesting
    CronBuilder addField(CronFieldName name, FieldExpression expression){
        FieldConstraints constraints = definition.getFieldDefinition(name).getConstraints();
        expression.accept(new ValidationFieldExpressionVisitor(constraints, definition.isStrictRanges()));
        fields.put(name, new CronField(name, expression, constraints));
        return this;
    }

    public static void main(String[] args) {
        Cron cron =
                CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                        .withYear(always())
                        .withDoM(between(SpecialChar.L, 3))
                        .withMonth(always())
                        .withDoW(questionMark())
                        .withHour(always())
                        .withMinute(always())
                        .withSecond(on(0))
                        .instance();
        System.out.println(cron.asString());
    }
}
