package com.cron.utils.model.field.definition;

import com.cron.utils.model.field.CronFieldName;
import com.cron.utils.model.field.constraint.FieldConstraints;
import com.cron.utils.model.field.definition.FieldDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FieldDefinitionTest {

    private CronFieldName testFieldName;
    @Mock
    private FieldConstraints mockConstraints;

    private FieldDefinition fieldDefinition;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;
        fieldDefinition = new FieldDefinition(testFieldName, mockConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldName(){
        new FieldDefinition(null, mockConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraints(){
        new FieldDefinition(testFieldName, null);
    }

    @Test
    public void testGetFieldName() throws Exception {
        assertEquals(testFieldName, fieldDefinition.getFieldName());
    }

    @Test
    public void testGetConstraints() throws Exception {
        assertEquals(mockConstraints, fieldDefinition.getConstraints());
    }

    @Test
    public void testCreateFieldDefinitionComparatorNotNull() throws Exception {
        assertNotNull(FieldDefinition.createFieldDefinitionComparator());
    }

    @Test
    public void testCreateFieldDefinitionComparatorEqual() throws Exception {
        CronFieldName name = CronFieldName.DAY_OF_MONTH;
        FieldDefinition fieldDefinition1 = new FieldDefinition(name, mockConstraints);
        FieldDefinition fieldDefinition2 = new FieldDefinition(name, mock(FieldConstraints.class));
        assertEquals(name.getOrder(), name.getOrder());
        assertEquals(0, FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition1, fieldDefinition2));
    }

    @Test
    public void testCreateFieldDefinitionComparatorGreater() throws Exception {
        CronFieldName name1 = CronFieldName.DAY_OF_MONTH;
        CronFieldName name2 = CronFieldName.SECOND;
        FieldDefinition fieldDefinition1 = new FieldDefinition(name1, mockConstraints);
        FieldDefinition fieldDefinition2 = new FieldDefinition(name2, mock(FieldConstraints.class));
        assertNotEquals(name1.getOrder(), name2.getOrder());
        assertTrue(FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition1, fieldDefinition2) > 0);
    }

    @Test
    public void testCreateFieldDefinitionComparatorLesser() throws Exception {
        CronFieldName name1 = CronFieldName.DAY_OF_MONTH;
        CronFieldName name2 = CronFieldName.SECOND;
        FieldDefinition fieldDefinition1 = new FieldDefinition(name1, mockConstraints);
        FieldDefinition fieldDefinition2 = new FieldDefinition(name2, mock(FieldConstraints.class));
        assertNotEquals(name1.getOrder(), name2.getOrder());
        assertTrue(FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition2, fieldDefinition1) < 0);
    }
}