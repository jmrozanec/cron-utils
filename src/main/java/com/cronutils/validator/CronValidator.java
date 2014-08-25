package com.cronutils.validator;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.parser.CronParser;
import org.apache.commons.lang3.Validate;

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
 * Validates if a cron expression conforms to the corresponding definition.
 */
public class CronValidator {

    private CronParser parser;

    /**
     * Constructor
     * @param definition - CronDefinition instance
     */
    public CronValidator(CronDefinition definition){
        this.parser = new CronParser(Validate.notNull(definition, "CronDefinition must not be null"));
    }

    /**
     * Validates cron expression
     * @param expression - string with cron expression
     * @return boolean - true if valid, false otherwise
     */
    public boolean isValid(String expression){
        try{
            validate(expression);
        }catch (RuntimeException re){
            return false;
        }
        return true;
    }

    /**
     * Validates cron expression
     * @param expression - string with cron expression
     * @return string with same cron expression as parameter
     * will raise a RuntimeException if the expression is invalid
     */
    public String validate(String expression){
        try{
            parser.parse(expression);
            return expression;
        }catch (RuntimeException re){
            throw new RuntimeException(String.format("Invalid cron expression: %s", expression), re);
        }
    }
}
