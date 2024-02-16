package com.cronutils.validation;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

public abstract class AbstractCronValidator {

    private CronType type;

    protected void initialize(CronType constraintAnnotation) {
        this.type = constraintAnnotation;
    }

    protected boolean isValid(String value) throws IllegalArgumentException {
        if (value == null) {
            return true;
        }
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(type);
        CronParser cronParser = new CronParser(cronDefinition);
        cronParser.parse(value).validate();
        return true;
    }
}
