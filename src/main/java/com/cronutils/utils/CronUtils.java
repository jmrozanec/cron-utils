package com.cronutils.utils;

import javax.ejb.ScheduleExpression;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronFieldName;

public class CronUtils {
    public static ScheduleExpression asScheduleExpression(Cron cron){
        if(cron.asString().contains("?")){
            throw new IllegalArgumentException("? not supported by ScheduleExpression");
        }
        ScheduleExpression expression = new ScheduleExpression();
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.YEAR)){
            expression.year(cron.retrieve(CronFieldName.YEAR).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.DAY_OF_YEAR)){
            throw new IllegalArgumentException("DoY not supported by ScheduleExpression");
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.DAY_OF_WEEK)){
            expression.dayOfWeek(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.MONTH)){
            expression.month(cron.retrieve(CronFieldName.MONTH).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.DAY_OF_MONTH)){
            expression.dayOfMonth(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.HOUR)){
            expression.hour(cron.retrieve(CronFieldName.HOUR).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.MINUTE)){
            expression.minute(cron.retrieve(CronFieldName.MINUTE).getExpression().asString());
        }
        if(cron.getCronDefinition().containsFieldDefinition(CronFieldName.SECOND)){
            expression.second(cron.retrieve(CronFieldName.SECOND).getExpression().asString());
        }
        return expression;
    }
}
