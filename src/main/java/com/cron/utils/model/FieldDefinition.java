package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.FieldConstraints;
import org.apache.commons.lang3.Validate;

import java.util.Comparator;

public class FieldDefinition {
    private CronFieldName fieldName;
    private FieldConstraints constraints;

    public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints){
        this.fieldName = Validate.notNull(fieldName, "CronFieldName must not be null");
        this.constraints = Validate.notNull(constraints, "FieldConstraints must not be null");
    }

    public CronFieldName getFieldName() {
        return fieldName;
    }

    public FieldConstraints getConstraints() {
        return constraints;
    }

    public static Comparator<FieldDefinition> createFieldDefinitionComparator() {
        return new Comparator<FieldDefinition>() {
            @Override
            public int compare(FieldDefinition o1, FieldDefinition o2) {
                return o1.getFieldName().getOrder() - o2.getFieldName().getOrder();
            }
        };
    }
}
