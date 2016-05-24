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
package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationFieldExpressionVisitor implements FieldExpressionVisitor {
    private static final String OORANGE = "Value %s not in range [%s, %s]";
    private FieldConstraints constraints;
    private Pattern stringToIntKeysPattern;
    private Pattern numsAndCharsPattern;
    private Pattern lwPattern;
    private boolean strictRanges;

    public ValidationFieldExpressionVisitor(FieldConstraints constraints, boolean strictRanges){
        this.constraints = constraints;
        this.lwPattern = buildLWPattern(constraints.getSpecialChars());
        this.stringToIntKeysPattern = buildStringToIntPattern(constraints.getStringMapping().keySet());
        this.numsAndCharsPattern = Pattern.compile("[#\\?/\\*0-9]");
        this.strictRanges = strictRanges;
    }

    @Override
    public FieldExpression visit(FieldExpression expression) {
        String unsupportedChars = removeValidChars(expression.asString()).toUpperCase();
        if("".equals(unsupportedChars)){
            if(expression instanceof Always){
                return visit((Always)expression);
            }
            if(expression instanceof And){
                return visit((And)expression);
            }
            if(expression instanceof Between){
                return visit((Between)expression);
            }
            if(expression instanceof Every){
                return visit((Every)expression);
            }
            if(expression instanceof On){
                return visit((On)expression);
            }
            if(expression instanceof QuestionMark){
                return visit((QuestionMark)expression);
            }
        }
        throw new RuntimeException(String.format("Expression contains unsupported chars: %s", unsupportedChars));
    }

    @Override
    public Always visit(Always always) {
        return always;
    }

    @Override
    public And visit(And and) {
        return and;
    }

    @Override
    public Between visit(Between between) {
        isInRange(between.getFrom());
        isInRange(between.getTo());
        if(isSpecialCharNotL(between.getFrom()) || isSpecialCharNotL(between.getTo())){
            throw new IllegalArgumentException("No special characters allowed in range, except for 'L'");
        }
        if(strictRanges){
            if(between.getFrom() instanceof IntegerFieldValue && between.getTo() instanceof IntegerFieldValue){
                int from = ((IntegerFieldValue)between.getFrom()).getValue();
                int to = ((IntegerFieldValue)between.getTo()).getValue();
                if(from>to){
                    throw new IllegalArgumentException(String.format("Invalid range! [%s,%s]", from, to));
                }
            }
        }
        return between;
    }

    @Override
    public Every visit(Every every) {
        if(every.getExpression() instanceof Between){
            visit((Between)every.getExpression());
        }
        if(every.getExpression() instanceof On){
            visit((On)every.getExpression());
        }
        isInRange(every.getPeriod());
        return every;
    }

    @Override
    public On visit(On on) {
        if(!isDefault(on.getTime())){
            isInRange(on.getTime());
        }
        if(!isDefault(on.getNth())){
            isInRange(on.getNth());
        }
        return on;
    }

    @Override
    public QuestionMark visit(QuestionMark questionMark) {
        return questionMark;
    }

    @VisibleForTesting
    Pattern buildStringToIntPattern(Set<String> strings){
        return buildWordsPattern(strings);
    }

    @VisibleForTesting
    String removeValidChars(String exp){
        Matcher numsAndCharsMatcher = numsAndCharsPattern.matcher(exp);
        Matcher stringToIntKeysMatcher = stringToIntKeysPattern.matcher(numsAndCharsMatcher.replaceAll(""));
        Matcher specialWordsMatcher = lwPattern.matcher(stringToIntKeysMatcher.replaceAll(""));
        return specialWordsMatcher.replaceAll("").replaceAll("\\s+", "").replaceAll(",", "").replaceAll("-", "");
    }

    @VisibleForTesting
    Pattern buildLWPattern(Set<SpecialChar> specialChars){
        Set<String> scs = Sets.newHashSet();
        for(SpecialChar sc : new SpecialChar[]{SpecialChar.L, SpecialChar.LW, SpecialChar.W}){
            if(specialChars.contains(sc)){
                scs.add(sc.name());
            }
        }
        return buildWordsPattern(scs);
    }

    @VisibleForTesting
    Pattern buildWordsPattern(Set<String> words){
        StringBuilder builder = new StringBuilder("\\b(");
        boolean first = true;
        for(String word : words){
            if(!first){
                builder.append("|");
            }else{
                first=false;
            }
            builder.append(word);
        }
        builder.append(")\\b");
        return Pattern.compile(builder.toString());
    }

    /**
     * Check if given number is greater or equal to start range and minor or equal to end range
     * @param fieldValue - to be validated
     * @throws - RuntimeException if not in range
     */
    @VisibleForTesting
    void isInRange(FieldValue fieldValue) {
        if(fieldValue instanceof IntegerFieldValue){
            int value = ((IntegerFieldValue)fieldValue).getValue();
            if (!constraints.isInRange(value)) {
                throw new IllegalArgumentException(String.format(OORANGE, value, constraints.getStartRange(), constraints.getEndRange()));
            }
        }
    }

    @VisibleForTesting
    boolean isDefault(FieldValue fieldValue) {
        if(fieldValue instanceof IntegerFieldValue){
            return ((IntegerFieldValue)fieldValue).getValue()==-1;
        }
        return false;
    }

    boolean isSpecialCharNotL(FieldValue fieldValue){
        if(fieldValue instanceof SpecialCharFieldValue){
            return !SpecialChar.L.equals(fieldValue.getValue());
        }
        return false;
    }
}
