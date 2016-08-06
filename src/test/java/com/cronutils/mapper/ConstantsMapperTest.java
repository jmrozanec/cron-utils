package com.cronutils.mapper;

import org.junit.Test;

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
public class ConstantsMapperTest {

    @Test
    public void testWeekDayMappingQuartzToJodatime() throws Exception {
        WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        WeekDay jodatime = ConstantsMapper.JAVA8;
        for(int j=2; j<8; j++){
            assertEquals(j-1, ConstantsMapper.weekDayMapping(quartz, jodatime, j));
        }
        assertEquals(7, ConstantsMapper.weekDayMapping(quartz, jodatime, 1));
    }

    @Test
    public void testWeekDayMappingJodatimeToQuartz() throws Exception {
        WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        WeekDay jodatime = ConstantsMapper.JAVA8;
        for(int j=1; j<7; j++){
            assertEquals(j+1, ConstantsMapper.weekDayMapping(jodatime, quartz, j));
        }
        assertEquals(1, ConstantsMapper.weekDayMapping(jodatime, quartz, 7));
    }

    @Test
    public void testWeekDayMappingQuartzToCrontab() throws Exception {
        WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        for(int j=1; j<7; j++){
            assertEquals(j-1, ConstantsMapper.weekDayMapping(quartz, crontab, j));
        }
    }

    @Test
    public void testWeekDayMappingCrontabToQuartz() throws Exception {
        WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        for(int j=0; j<7; j++){
            assertEquals(j+1, ConstantsMapper.weekDayMapping(crontab, quartz, j));
        }
    }

    @Test
    public void testWeekDayMappingCrontabToJodatime() throws Exception {
        WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        WeekDay jodatime = ConstantsMapper.JAVA8;
        for(int j=1; j<7; j++){
            assertEquals(j, ConstantsMapper.weekDayMapping(crontab, jodatime, j));
        }
        assertEquals(7, ConstantsMapper.weekDayMapping(crontab, jodatime, 0));
    }

    @Test
    public void testWeekDayMappingJodatimeToCrontab() throws Exception {
        WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        WeekDay jodatime = ConstantsMapper.JAVA8;
        for(int j=1; j<7; j++){
            assertEquals(j, ConstantsMapper.weekDayMapping(jodatime, crontab, j));
        }
        assertEquals(0, ConstantsMapper.weekDayMapping(jodatime, crontab, 7));
    }
}