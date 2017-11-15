package com.cronutils.model.time.generator;

import java.util.ArrayList;
import java.util.List;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.utils.Preconditions;

class AndDayOfWeekValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;
    private WeekDay mondayDoWValue;

    public AndDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
        this.mondayDoWValue = mondayDoWValue;
    }

    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = new ArrayList<>();
        And and = (And) cronField.getExpression();

        for (FieldExpression expression : and.getExpressions()) {
            CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK, expression, this.cronField.getConstraints());
            List<Integer> candidatesList = FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(
                    cronField, year, month, mondayDoWValue
            ).generateCandidates(start, end);

            // add them to the master list
            if (candidatesList != null) {
                values.addAll(candidatesList);
            }
        }

        return values;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof And;
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public boolean isMatch(int value) {
        // This method does not logically work.
        return false;
    }

}
