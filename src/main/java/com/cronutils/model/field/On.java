package com.cronutils.model.field;

import com.cronutils.model.field.constraint.FieldConstraints;

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
public class On extends FieldExpression {
    private int time;
    private int nth;
    private SpecialChar specialChar;

    public On(FieldConstraints constraints, String exp) {
        super(constraints);
        nth = -1;
        specialChar = SpecialChar.NONE;
        time = getConstraints().validateInRange(
                getConstraints().intToInt(
                        getConstraints().stringToInt(
                                retrieveSpecialChar(getConstraints(), exp)
                        )
                )
        );
    }

    public int getTime() {
        return time;
    }

    public int getNth() {
        return nth;
    }

    public SpecialChar getSpecialChar() {
        return specialChar;
    }

    private String retrieveSpecialChar(FieldConstraints constraints, String exp) {
        String expression = exp;
        if (exp.contains("#")) {
            specialChar = SpecialChar.HASH;
            String[] array = exp.split("#");
            nth = constraints.validateInRange(constraints.intToInt(constraints.stringToInt(array[1])));
            if (array[0].isEmpty()) {
                throw new RuntimeException("Time should be specified!");
            }
            expression = array[0];
        }
        if (exp.contains("LW")) {
            specialChar = SpecialChar.LW;
            exp = exp.replace("LW", "");
            if ("".equals(exp)) {
                expression = "0";//to avoid a NumberFormatException
            } else {
                expression = exp;
            }
        }
        if (exp.contains("L")) {
            specialChar = SpecialChar.L;
            exp = exp.replace("L", "");
            if ("".equals(exp)) {
                expression = "0";//to avoid a NumberFormatException
            } else {
                expression = exp;
            }
        }
        if (exp.contains("W")) {
            specialChar = SpecialChar.W;
            expression = exp.replace("W", "");
        }
        constraints.validateSpecialCharAllowed(specialChar);
        return expression;
    }

    @Override
    public String asString() {
        switch (specialChar){
            case NONE:
                return ""+getTime();
            case HASH:
                return String.format("%s#%s", getTime(), getNth());
            case W:
                return String.format("%sW", getTime());
            default:
                return specialChar.toString();
        }
    }
}
