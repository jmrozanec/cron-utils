package com.cronutils.model.time.generator;

import com.cronutils.model.field.FieldExpression;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FieldValueGeneratorTest {

    private FieldValueGenerator fieldValueGenerator;

    @Before
    public void setUp(){
        fieldValueGenerator = new MockFieldValueGenerator(mock(FieldExpression.class));
    }

    @Test
    public void testGenerateCandidates() throws Exception {
        int start = 1;
        int end = 2;
        assertTrue(fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(start, end).isEmpty());
        List<Integer> candidates = fieldValueGenerator.generateCandidates(1, 2);
        assertFalse(candidates.isEmpty());
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains(start));
        assertTrue(candidates.contains(end));
    }
}