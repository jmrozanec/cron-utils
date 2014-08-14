package com.cron.utils.descriptor;

import com.cron.utils.parser.field.*;
import com.google.common.base.Function;
import org.apache.commons.lang3.Validate;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

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
 * Description strategy to handle cases on how to present
 * cron information in a human readable format
 */
abstract class DescriptionStrategy {
    protected Function<Integer, String> nominalValueFunction;
    protected ResourceBundle bundle;

    public DescriptionStrategy(ResourceBundle bundle) {
        this.bundle = bundle;
        nominalValueFunction = new Function<Integer, String>() {
            public String apply(Integer integer) {
                return "" + integer;
            }
        };
    }

    /**
     * Provide a human readable description;
     * @return human readable description - String
     */
    public abstract String describe();

    /**
     * Given a CronFieldExpression, provide a String with a human readable description.
     * Will identify CronFieldExpression subclasses and delegate.
     * @param fieldExpression - CronFieldExpression instance - not null
     * @return human readable description - String
     */
    protected String describe(FieldExpression fieldExpression) {
        Validate.notNull(fieldExpression, "CronFieldExpression should not be null!");
        if (fieldExpression instanceof Always) {
            return describe((Always) fieldExpression);
        }
        if (fieldExpression instanceof And) {
            return describe((And) fieldExpression);
        }
        if (fieldExpression instanceof Between) {
            return describe((Between) fieldExpression);
        }
        if (fieldExpression instanceof Every) {
            return describe((Every) fieldExpression);
        }
        if (fieldExpression instanceof On) {
            return describe((On) fieldExpression);
        }
        return "";
    }

    /**
     * Given an int, will return a nominal value. Example:
     * 1 in weeks context, may mean "Monday",
     * so nominal value for 1 would be "Monday"
     * Default will return int as String
     * @param value - some integer
     * @return String
     */
    protected String nominalValue(int value) {
        return nominalValueFunction.apply(value);
    }

    /**
     * Provide a human readable description for Always instance
     * @param always - Always
     * @return human readable description - String
     */
    protected String describe(Always always) {
        if (always.getEvery().getTime() <= 1) {
            return "";
        }
        return describe(always.getEvery());
    }

    /**
     * Provide a human readable description for And instance
     * @param and - And
     * @return human readable description - String
     */
    protected String describe(And and) {
        List<FieldExpression> expressions = and.getExpressions();
        StringBuilder builder = new StringBuilder();
        builder.append(describe(expressions.get(0)));
        for (int j = 1; j < expressions.size(); j++) {
            builder.append(String.format(" %s %s ", bundle.getString("and"), describe(expressions.get(j))));
        }
        return builder.toString();
    }

    /**
     * Provide a human readable description for Between instance
     * @param between - Between
     * @return human readable description - String
     */
    protected String describe(Between between) {
        return new StringBuilder()
                .append(describe(between.getEvery()))
                .append(MessageFormat.format(bundle.getString("between_x_and_y"), nominalValue(between.getFrom()), nominalValue(between.getTo())))
                .append(" ").toString();
    }

    /**
     * Provide a human readable description for Every instance
     * @param every - Every
     * @return human readable description - String
     */
    protected String describe(Every every) {
        if (every.getTime() > 1) {
            return String.format("%s %s ", bundle.getString("every"), nominalValue(every.getTime())) + "%s";
        }
        return bundle.getString("every") + " %s ";
    }

    /**
     * Provide a human readable description for On instance
     * @param on - On
     * @return human readable description - String
     */
    protected String describe(On on) {
        return String.format("%s %s ", bundle.getString("at"), nominalValue(on.getTime())) + "%s";
    }
}
