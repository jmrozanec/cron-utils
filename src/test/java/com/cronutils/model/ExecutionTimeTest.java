package com.cronutils.model;

import com.cronutils.model.field.Always;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.On;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ExecutionTimeTest {
    private ExecutionTime executionTime;

    @Before
    public void setUp(){
        Map<CronFieldName, CronField> fields = Maps.newHashMap();
        fields.put(CronFieldName.SECOND, createCronField(CronFieldName.SECOND));
        fields.put(CronFieldName.MINUTE, createCronField(CronFieldName.MINUTE));
        fields.put(CronFieldName.HOUR, createCronField(CronFieldName.HOUR));
        fields.put(CronFieldName.DAY_OF_MONTH, createCronField(CronFieldName.DAY_OF_MONTH));
        fields.put(CronFieldName.DAY_OF_WEEK, createCronField(CronFieldName.DAY_OF_WEEK));
        fields.put(CronFieldName.MONTH, createCronField(CronFieldName.MONTH));
        fields.put(CronFieldName.YEAR, createCronField(CronFieldName.YEAR));
        executionTime = new ExecutionTime(fields);
    }

    private CronField createCronField(CronFieldName name){
        return new CronField(
                name,
                new Always(
                        FieldConstraintsBuilder.instance()
                                .forField(name)
                                .createConstraintsInstance()
                )
        );
    }

    @Test
    public void testNextValue() throws Exception {

    }

    @Test
    public void testFromFieldToTimeValues() throws Exception {

    }

    @Test
    public void testFromFieldToTimeValuesAnd() throws Exception {

    }

    @Test
    public void testFromFieldToTimeValuesBetween() throws Exception {

    }

    @Test
    public void testFromFieldToTimeValuesOn() throws Exception {

    }

    @Test
    public void testFromFieldToTimeValuesAlways() throws Exception {

    }

    @Test
    public void testGetMaxForCronFieldYear() throws Exception {
        assertEquals(DateTime.now().getYear()+1, executionTime.getMaxForCronField(CronFieldName.YEAR));
    }

    @Test
    public void testGetMaxForCronFieldMonth() throws Exception {
        assertEquals(12, executionTime.getMaxForCronField(CronFieldName.MONTH));
    }

    @Test
    public void testGetMaxForCronFieldDoW() throws Exception {
        assertEquals(7, executionTime.getMaxForCronField(CronFieldName.DAY_OF_WEEK));
    }

    @Test
    public void testGetMaxForCronFieldDoM() throws Exception {
        assertEquals(31, executionTime.getMaxForCronField(CronFieldName.DAY_OF_MONTH));
    }

    @Test
    public void testGetMaxForCronFieldHour() throws Exception {
        assertEquals(24, executionTime.getMaxForCronField(CronFieldName.HOUR));
    }

    @Test
    public void testGetMaxForCronFieldMinute() throws Exception {
        assertEquals(60, executionTime.getMaxForCronField(CronFieldName.MINUTE));
    }

    @Test
    public void testGetMaxForCronFieldSecond() throws Exception {
        assertEquals(60, executionTime.getMaxForCronField(CronFieldName.SECOND));
    }
}