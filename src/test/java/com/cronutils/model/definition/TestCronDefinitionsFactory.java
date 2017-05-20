package com.cronutils.model.definition;
/*
 * Copyright 2017 jmrozanec
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
public class TestCronDefinitionsFactory {
    private static final int LEAP_YEAR_DAY_COUNT = 366;

    public static CronDefinition withDayOfYearDefinition(){
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 5000).and()
                .withDayOfYear().supportsQuestionMark().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }

    public static CronDefinition withDayOfYearDefinitionWhereYearAndDoYOptionals(){
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 5000).optional().and()
                .withDayOfYear().supportsQuestionMark().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }
}
