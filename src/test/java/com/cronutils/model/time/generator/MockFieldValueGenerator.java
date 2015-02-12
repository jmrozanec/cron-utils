package com.cronutils.model.time.generator;

import com.cronutils.model.field.FieldExpression;
import com.google.common.collect.Lists;

import java.util.List;

public class MockFieldValueGenerator extends FieldValueGenerator {

    public MockFieldValueGenerator(FieldExpression expression) {
        super(expression);
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        return 0;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        return 0;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isMatch(int value) {
        return true;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return true;
    }
}
