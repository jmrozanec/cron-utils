package com.cronutils.descriptor.refactor;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang3.Validate;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

class FieldDescriptor {
    protected ResourceBundle bundle;
    private MessageFormat formatter;

    public FieldDescriptor(ResourceBundle bundle) {
        this.bundle = bundle;
        formatter = new MessageFormat("", Locale.US);//TODO extract
    }

    protected String describe(CronField previous, CronField current, Range<Integer> range) {
        Validate.notNull(current, "CronField must not be null!");
        FieldExpression pexpression = null;
        if (previous != null) {
            pexpression = previous.getExpression();
        }
        FieldExpression expression = current.getExpression();
        switch (current.getField()) {
            case YEAR:
                return describeYear(pexpression, expression, range);
            case MONTH:
                return describeMonth(pexpression, expression, range);
            case DAY_OF_MONTH:
                return describeDayOfMonth(pexpression, expression, range);
            case DAY_OF_WEEK:
                return describeDayOfWeek(pexpression, expression, range);
            case HOUR:
                return describeHour(pexpression, expression, range);
            case MINUTE:
                return describeMinute(pexpression, expression, range);
            case SECOND:
                return describeSecond(pexpression, expression, range);
        }
        return "";
    }

    protected String describeYear(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_year", "between_x_and_y_year", "every_x_years", "on_x_year");
    }

    protected String describeMonth(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_month", "between_x_and_y_month", "every_x_months", "on_x_month");//TODO add nominal transform
    }

    protected String describeDayOfMonth(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_dom", "between_x_and_y_dom", "every_x_dom", "on_x_dom");
    }

    protected String describeDayOfWeek(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_dow", "between_x_and_y_dow", "every_x_dow", "on_x_dow");
    }

    protected String describeHour(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_hour", "between_x_and_y_hour", "every_x_hour", "on_x_hour");
    }

    protected String describeMinute(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_minute", "between_x_and_y_minute", "every_x_minute", "on_x_minute");
    }

    protected String describeSecond(FieldExpression previous, FieldExpression current, Range<Integer> range) {
        return describe(previous, current, range, "every_second", "between_x_and_y_second", "every_x_second", "on_x_second");
    }

    protected String describe(FieldExpression previous, FieldExpression current, Range<Integer> range,
                              String always, String between, String everyX, String onX) {
        if (current instanceof Always) {
            return describeAlways(previous, current, always);
        }
        if (current instanceof Between) {
            return describeBetween(previous, (Between)current, between);
        }
        if (current instanceof Every) {
            if(((Every)current).getTime().getValue()==1) {
                return describeAlways(previous, current, always);
            }else {
                return describeEveryX(previous, (Every)current, everyX);
            }
        }
        if (current instanceof On) {
            if(((On)current).getTime().getValue()==0){
                return "";
            }
            return describeOnX(previous, (On)current, onX);
        }
        if (current instanceof And) {
            And and = (And)current;
            StringBuilder builder = new StringBuilder();
            RangeSet<Integer> ranges = TreeRangeSet.create();
            List<SpecialChar> onSpecialCharValues = Lists.newArrayList();
            for(FieldExpression expression : and.getExpressions()){
                if(expression instanceof On){
                    On on = (On)expression;
                    if(SpecialChar.NONE.equals(on.getSpecialChar().getValue())){
                        ranges.add(Range.closed(on.getTime().getValue(), on.getTime().getValue()));
                    }else{
                        onSpecialCharValues.add(on.getSpecialChar().getValue());
                    }
                }
                if(expression instanceof Between){
                    Between betweenExp = (Between)expression;
                    if(betweenExp.getFrom() instanceof IntegerFieldValue && betweenExp.getTo() instanceof IntegerFieldValue){
                        int from = ((IntegerFieldValue)betweenExp.getFrom()).getValue();
                        int to = ((IntegerFieldValue)betweenExp.getTo()).getValue();
                        int every = betweenExp.getEvery().getTime().getValue();
                        if(every==1){
                            ranges.add(Range.closed(from, to));
                        }else{
                            for(int j=from; j<=to; j+=every){
                                ranges.add(Range.closed(j, j));
                            }
                        }
                    } else {
                        SpecialChar specialChar;
                        int from;
                        if(betweenExp.getFrom() instanceof SpecialCharFieldValue){
                            specialChar = ((SpecialCharFieldValue)betweenExp.getFrom()).getValue();
                            from = ((IntegerFieldValue)betweenExp.getTo()).getValue();
                        } else {
                            from = ((IntegerFieldValue)betweenExp.getFrom()).getValue();
                            specialChar = ((SpecialCharFieldValue)betweenExp.getTo()).getValue();
                        }
                        //TODO manage case where one of values is special char
                    }
                }
                if(expression instanceof Every){
                    Every every = (Every)expression;
                    if(every.getTime().getValue()==1){
                        return describeAlways(previous, current, always);
                    }else{
                        //TODO we need the range, to calculate times in range
                        ranges.add(Range.closed(every.getTime().getValue(), every.getTime().getValue()));
                    }
                }
                Set<Range<Integer>> sortedDisconnectedRanges = ranges.asRanges();
                if(sortedDisconnectedRanges.size()==1 &&//TODO update Range.closed(1, 1) with correct value
                        range.equals(sortedDisconnectedRanges.iterator().next())){
                    return always;
                }
                for(Range consolidatedRange : sortedDisconnectedRanges){
                    //TODO describe ranges
                }
            }
        }
        return "";
    }

    protected String describeAlways(FieldExpression previous, FieldExpression current, String always){
        if(previous instanceof Always || previous instanceof Every){
            return "";
        }
        return bundle.getString(always);
    }

    protected String describeBetween(FieldExpression previous, Between current, String between){
        formatter.applyPattern(bundle.getString(between));
        return formatter.format(new Object[]{current.getFrom().getValue(), current.getTo().getValue()});
    }

    protected String describeEveryX(FieldExpression previous, Every current, String everyX){
        formatter.applyPattern(bundle.getString(everyX));
        return formatter.format(new Object[]{current.getTime()});
    }

    protected String describeOnX(FieldExpression previous, On current, String onX){
        formatter.applyPattern(bundle.getString(onX));
        return formatter.format(new Object[]{current.getTime()});
    }
}
