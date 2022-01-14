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

package com.cronutils.mapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstantsMapperTest {

    @Test
    public void testWeekDayMappingQuartzToJDK8time() {
        final WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        final WeekDay jdktime = ConstantsMapper.JAVA8;
        for (int j = 2; j < 8; j++) {
            assertEquals(j - 1L, ConstantsMapper.weekDayMapping(quartz, jdktime, j));
        }
        assertEquals(7, ConstantsMapper.weekDayMapping(quartz, jdktime, 1));
    }

    @Test
    public void testWeekDayMappingJDK8ToQuartz() {
        final WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        final WeekDay jdktime = ConstantsMapper.JAVA8;
        for (int j = 1; j < 7; j++) {
            assertEquals(j + 1L, ConstantsMapper.weekDayMapping(jdktime, quartz, j));
        }
        assertEquals(1, ConstantsMapper.weekDayMapping(jdktime, quartz, 7));
    }

    @Test
    public void testWeekDayMappingQuartzToCrontab() {
        final WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        final WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        for (int j = 1; j < 7; j++) {
            assertEquals(j - 1L, ConstantsMapper.weekDayMapping(quartz, crontab, j));
        }
    }

    @Test
    public void testWeekDayMappingCrontabToQuartz() {
        final WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        final WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        for (int j = 0; j < 7; j++) {
            assertEquals(j + 1L, ConstantsMapper.weekDayMapping(crontab, quartz, j));
        }
    }

    @Test
    public void testWeekDayMappingCrontabToJDK8() {
        final WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        final WeekDay jdktime = ConstantsMapper.JAVA8;
        for (int j = 1; j < 7; j++) {
            assertEquals(j, ConstantsMapper.weekDayMapping(crontab, jdktime, j));
        }
        assertEquals(7, ConstantsMapper.weekDayMapping(crontab, jdktime, 0));
    }

    @Test
    public void testWeekDayMappingJDK8ToCrontab() {
        final WeekDay crontab = ConstantsMapper.CRONTAB_WEEK_DAY;
        final WeekDay jdktime = ConstantsMapper.JAVA8;
        for (int j = 1; j < 7; j++) {
            assertEquals(j, ConstantsMapper.weekDayMapping(jdktime, crontab, j));
        }
        assertEquals(0, ConstantsMapper.weekDayMapping(jdktime, crontab, 7));
    }
}
