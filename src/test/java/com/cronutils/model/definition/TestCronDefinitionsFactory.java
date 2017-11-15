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

    /**
     * Provides a <code>CronDefinition</code> that extends the Quartz Cron definition by an optional DoY field at the end.
     * <p>
     * The cron expression is expected to be a string comprised of 6, 7 or 8
     * fields separated by white space. Fields can contain any of the allowed
     * values, along with various combinations of the allowed special characters
     * for that field. The fields are as follows:
     * <p>
     * <table style="width:100%">
     * <tr>
     * <th>Field Name</th>
     * <th>Mandatory</th>
     * <th>Allowed Values</th>
     * <th>Allowed Special Characters</th>
     * </tr>
     * <tr>
     * <td>Seconds</td>
     * <td>YES</td>
     * <td>0-59</td>
     * <td>* , - /</td>
     * </tr>
     * <tr>
     * <td>Minutes</td>
     * <td>YES</td>
     * <td>0-59</td>
     * <td>* , - /</td>
     * </tr>
     * <tr>
     * <td>Hours</td>
     * <td>YES</td>
     * <td>0-23</td>
     * <td>* , - /</td>
     * </tr>
     * <tr>
     * <td>Day of month</td>
     * <td>YES</td>
     * <td>1-31</td>
     * <td>* ? , - / L W</td>
     * </tr>
     * <tr>
     * <td>Month</td>
     * <td>YES</td>
     * <td>1-12 or JAN-DEC</td>
     * <td>* , -</td>
     * </tr>
     * <tr>
     * <td>Day of week</td>
     * <td>YES</td>
     * <td>1-7 or SUN-SAT</td>
     * <td>* ? , - / L #</td>
     * </tr>
     * <tr>
     * <td>Year</td>
     * <td>NO (if last field) YES (otherwise)</td>
     * <td>empty, 1970-2099</td>
     * <td>* , - /</td>
     * </tr>
     * <tr>
     * <td>Day of year</td>
     * <td>NO</td>
     * <td>empty, 1-366</td>
     * <td>* ? , - /</td>
     * </tr>
     * </table>
     * <P>
     * Thus in general cron expressions are as follows:
     * <p>
     * S M H DoM M DoW [Y [DoY]]
     *
     * @return the newly created <code>CronDefinition</code>.
     */
    public static CronDefinition withDayOfYearDefinitionWhereYearAndDoYOptionals() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).optional().and()
                .withDayOfYear().supportsQuestionMark().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }
}
