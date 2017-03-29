package com.cronutils.utils.descriptor;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
/*
 * Copyright 2015 jmrozanec
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
@RunWith(AndroidJUnit4.class)
public class CronDescriptorQuartzIntegrationTest {

    private CronDescriptor descriptor;
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testCronWithAndHours(){
        assertExpression("* * 1,2,3,4 * * ? *", "at 1, 2, 3 and 4 hours");
    }

    @Test
    public void testCronAndWithRangeHours(){
        assertExpression("* * 1,2,3,4,6-9 * * ? *", "at 1, 2, 3 and 4 hours and every hour between 6 and 9");
    }

    @Test
    public void testCronAndWithRangesAndEveryExpressions(){
        assertExpression("0 2-59/3 1,9,22 11-26 1-6 ?", "every 3 minutes between 2 and 59 at 1, 9 " +
                "and 22 hours every day between 11 and 26 every month between January and June");
    }

    @Test
    public void testEverySecond() {
        assertExpression("* * * * * ?", "every second");
    }

    @Test
    public void testEvery45Seconds(){
        assertExpression("*/45 * * * * ?", "every 45 seconds");
    }

    @Test
    public void testEveryHour(){
        assertExpression("0 0 * * * ?", "every hour");
        assertExpression("0 0 0/1 * * ?", "every hour");
    }

    /* Examples exposed at cron documentations */
    @Test
    public void testEveryDayFireAtNoon() throws Exception {
        assertExpression("0 0 12 * * ?", "at 12:00");
    }

    @Test
    public void testEveryDayFireAtTenFifteen() throws Exception {
        String description = "at 10:15";
        assertExpression("0 15 10 ? * *", description);
        assertExpression("0 15 10 * * ?", description);
        assertExpression("0 15 10 * * ? *", description);
    }

    @Test
    public void testEveryDayFireAtTenFifteenYear2005() throws Exception {
        assertExpression("0 15 10 * * ? 2005", "at 10:15 at 2005 year");
    }

    @Test
    public void testEveryMinuteBetween14and15EveryDay() throws Exception {
        assertExpression("0 * 14 * * ?", "at 14 hour");
    }

    @Test
    public void testEveryFiveMinutesBetween14and15EveryDay() throws Exception {
        assertExpression("0 0/5 14 * * ?", "every 5 minutes at 14 hour");
    }

    @Test
    public void testEveryFiveMinutesBetween14and15AndBetween18And19EveryDay() throws Exception {
        assertExpression("0 0/5 14,18 * * ?", "every 5 minutes at 14 and 18 hours");
    }

    /**
     * Issue #43: getting bad description for expression
     * @throws Exception
     */
    //TODO enable
    public void testEveryDayEveryFourHoursFromHour2() throws Exception {
        assertExpression("0 0 2/4 * * ?", "");
    }
    
    /*
     * Issue #103
     */
    //TODO enable
    public void testDescriptionDayOfWeek() {
        assertExpression("* 0/1 * ? * TUE", "every minute at Tuesday day");
    }

    private void assertExpression(String cron, String description){
        assertEquals(description, descriptor.describe(parser.parse(cron)));
    }
}
