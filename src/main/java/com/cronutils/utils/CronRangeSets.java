package com.cronutils.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class CronRangeSets {
    private Map<CronFieldName, RangeSet<Integer>> rangesets = new HashMap<>();

    public CronRangeSets(Cron cron){
        for(Map.Entry<CronFieldName, CronField> entry : cron.retrieveFieldsAsMap().entrySet()){
            FieldConstraints constraints = entry.getValue().getConstraints();
            RangeSetExpressionVisitor visitor = new RangeSetExpressionVisitor(constraints.getStartRange(), constraints.getEndRange());
            entry.getValue().getExpression().accept(visitor);
            rangesets.put(entry.getKey(), visitor.getRangeset());
        }
    }

    public Map<CronFieldName, RangeSet<Integer>> getRangesets(){
        return Collections.unmodifiableMap(rangesets);
    }

    private static class RangeSetExpressionVisitor implements FieldExpressionVisitor {
        private RangeSet<Integer> rangeset = TreeRangeSet.create();
        private int startrange;
        private int endrange;

        public RangeSetExpressionVisitor(int startrange, int endrange){
            this.startrange = startrange;
            this.endrange = endrange;
        }

        public RangeSet<Integer> getRangeset(){
            return ImmutableRangeSet.copyOf(rangeset);
        }

        @Override
        public FieldExpression visit(FieldExpression expression) {
            if(expression instanceof Always){
                visit((Always) expression);
            }
            if(expression instanceof And){
                visit((And)expression);
            }
            if(expression instanceof Between){
                visit((Between) expression);
            }
            if(expression instanceof Every){
                visit((Every) expression);
            }
            if(expression instanceof On){
                visit((On)expression);
            }
            if(expression instanceof QuestionMark){
                visit((QuestionMark) expression);
            }
            return expression;
        }

        @Override
        public FieldExpression visit(Always always) {
            rangeset.add(Range.closed(startrange, endrange));
            return always;
        }

        @Override
        public FieldExpression visit(And and) {
            for(FieldExpression expression : and.getExpressions()){
                visit(expression);
            }
            return and;
        }

        @Override
        public FieldExpression visit(Between between) {
            if(between.getFrom() instanceof IntegerFieldValue && between.getTo() instanceof IntegerFieldValue){
                rangeset.add(Range.closed(((IntegerFieldValue)between.getFrom()).getValue(), ((IntegerFieldValue)between.getTo()).getValue()));
            }else{
                throw new IllegalArgumentException("Special characters depend on contextual values and are currently not supported by RangeSet mapping");
            }
            return between;
        }

        @Override
        public FieldExpression visit(Every every) {
            int period = every.getPeriod().getValue();
            int start = startrange;
            int end = endrange;
            if(every.getExpression() instanceof Between){
                Between between = (Between) every.getExpression();
                if(between.getFrom() instanceof IntegerFieldValue && between.getTo() instanceof IntegerFieldValue){
                    start = ((IntegerFieldValue)between.getFrom()).getValue();
                    end = ((IntegerFieldValue)between.getTo()).getValue();
                }else{
                    throw new IllegalArgumentException("Special characters depend on contextual values and are currently not supported by RangeSet mapping");
                }
            }
            if(every.getExpression() instanceof On){
                On on = (On) every.getExpression();
                IntegerFieldValue time = on.getTime();
                SpecialCharFieldValue specchar = on.getSpecialChar();
                switch (specchar.getValue()){
                    case L:
                        throw new IllegalArgumentException("L depends on contextual values and is currently not supported by RangeSet mapping");
                    case W:
                        throw new IllegalArgumentException("W depends on contextual values and is currently not supported by RangeSet mapping");
                    case LW:
                        throw new IllegalArgumentException("LW depends on contextual values and is currently not supported by RangeSet mapping");
                    case HASH:
                        throw new IllegalArgumentException("# depends on contextual values and is currently not supported by RangeSet mapping");
                    case NONE:
                        start = time.getValue();
                }
            }
            if(every.getExpression() instanceof QuestionMark){
                throw new IllegalArgumentException("? not currently not supported for RangeSet mapping");
            }

            do{
                rangeset.add(Range.singleton(start));
                start+=period;
            }while(start < end);

            return every;
        }

        @Override
        public FieldExpression visit(On on) {
            IntegerFieldValue time = on.getTime();
            SpecialCharFieldValue specchar = on.getSpecialChar();
            switch (specchar.getValue()){
                case L:
                    throw new IllegalArgumentException("L depends on contextual values and is currently not supported by RangeSet mapping");
                case W:
                    throw new IllegalArgumentException("W depends on contextual values and is currently not supported by RangeSet mapping");
                case LW:
                    throw new IllegalArgumentException("LW depends on contextual values and is currently not supported by RangeSet mapping");
                case HASH:
                    throw new IllegalArgumentException("# depends on contextual values and is currently not supported by RangeSet mapping");
                case NONE:
                    rangeset.add(Range.singleton(time.getValue()));
            }
            return on;
        }

        @Override
        public FieldExpression visit(QuestionMark questionMark) {
            //we do not add any range!
            return questionMark;
        }
    }
}
