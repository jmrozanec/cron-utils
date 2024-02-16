/*
 * Copyright 2014 jmrozanec
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

package com.cronutils.model.definition;

import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.*;

import java.util.*;

/**
 * Builder that allows to define and create CronDefinition instances.
 */
public class CronDefinitionBuilder {
    private final Map<CronFieldName, FieldDefinition> fields = new EnumMap<>(CronFieldName.class);
    private final Set<CronConstraint> cronConstraints = new HashSet<>();
    private final Set<CronNicknames> cronNicknames = new HashSet<>();
    private boolean matchDayOfWeekAndDayOfMonth;

    /**
     * Constructor.
     */
    private CronDefinitionBuilder() {/*NOP*/}

    /**
     * Creates a builder instance.
     *
     * @return new CronDefinitionBuilder instance
     */
    public static CronDefinitionBuilder defineCron() {
        return new CronDefinitionBuilder();
    }

    /**
     * Adds definition for seconds field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withSeconds() {
        return new FieldDefinitionBuilder(this, CronFieldName.SECOND);
    }

    /**
     * Adds definition for minutes field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMinutes() {
        return new FieldDefinitionBuilder(this, CronFieldName.MINUTE);
    }

    /**
     * Adds definition for hours field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withHours() {
        return new FieldDefinitionBuilder(this, CronFieldName.HOUR);
    }

    /**
     * Adds definition for day of month field.
     *
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder withDayOfMonth() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_MONTH);
    }

    /**
     * Adds definition for month field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMonth() {
        return new FieldDefinitionBuilder(this, CronFieldName.MONTH);
    }

    /**
     * Adds definition for day of week field.
     *
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withDayOfWeek() {
        return new FieldDayOfWeekDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
    }

    /**
     * Adds definition for year field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withYear() {
        return new FieldDefinitionBuilder(this, CronFieldName.YEAR);
    }

    /**
     * Adds definition for day of year field.
     *
     * @return new FieldDefinitionBuilder instance
     */
    public FieldQuestionMarkDefinitionBuilder withDayOfYear() {
        return new FieldQuestionMarkDefinitionBuilder(this, CronFieldName.DAY_OF_YEAR);
    }

    /**
     * Sets matchDayOfWeekAndDayOfMonth value to true.
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder matchDayOfWeekAndDayOfMonth() {
        matchDayOfWeekAndDayOfMonth = true;
        return this;
    }

    /**
     * Supports cron nickname @yearly
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameYearly() {
        cronNicknames.add(CronNicknames.YEARLY);
        return this;
    }

    /**
     * Supports cron nickname @annually
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameAnnually() {
        cronNicknames.add(CronNicknames.ANNUALLY);
        return this;
    }

    /**
     * Supports cron nickname @monthly
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameMonthly() {
        cronNicknames.add(CronNicknames.MONTHLY);
        return this;
    }

    /**
     * Supports cron nickname @weekly
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameWeekly() {
        cronNicknames.add(CronNicknames.WEEKLY);
        return this;
    }

    /**
     * Supports cron nickname @daily
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameDaily() {
        cronNicknames.add(CronNicknames.DAILY);
        return this;
    }

    /**
     * Supports cron nickname @midnight
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameMidnight() {
        cronNicknames.add(CronNicknames.MIDNIGHT);
        return this;
    }

    /**
     * Supports cron nickname @hourly
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameHourly() {
        cronNicknames.add(CronNicknames.HOURLY);
        return this;
    }

    /**
     * Supports cron nickname @reboot
     *
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withSupportedNicknameReboot() {
        cronNicknames.add(CronNicknames.REBOOT);
        return this;
    }

    /**
     * Adds a cron validation.
     * @param validation - constraint validation
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder withCronValidation(final CronConstraint validation) {
        cronConstraints.add(validation);
        return this;
    }

    /**
     * Registers a certain FieldDefinition.
     *
     * @param definition - FieldDefinition  instance, never null
     */
    public void register(final FieldDefinition definition) {
        //ensure that we can't register a mandatory definition if there are already optional ones
        boolean hasOptionalField = false;
        for (final FieldDefinition fieldDefinition : fields.values()) {
            if (fieldDefinition.isOptional()) {
                hasOptionalField = true;
                break;
            }
        }
        if (!definition.isOptional() && hasOptionalField) {
            throw new IllegalArgumentException("Can't register mandatory definition after a optional definition.");
        }
        fields.put(definition.getFieldName(), definition);
    }

    /**
     * Creates a new CronDefinition instance with provided field definitions.
     *
     * @return returns CronDefinition instance, never null
     */
    public CronDefinition instance() {
        final Set<CronConstraint> validations = new HashSet<>(cronConstraints);
        final List<FieldDefinition> values = new ArrayList<>(fields.values());
        values.sort(FieldDefinition.createFieldDefinitionComparator());
        return new CronDefinition(values, validations, cronNicknames, matchDayOfWeekAndDayOfMonth);
    }

    /**
     * Creates CronDefinition instance matching cron4j specification.
     *
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition cron4j() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(0, 31).supportsL().withStrictRange().and()
                .withMonth().withValidRange(1, 12).withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 6).withMondayDoWValue(1).withStrictRange().and()
                .matchDayOfWeekAndDayOfMonth()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching Quartz specification.
     *
     * <p>The cron expression is expected to be a string comprised of 6 or 7
     * fields separated by white space. Fields can contain any of the allowed
     * values, along with various combinations of the allowed special characters
     * for that field. The fields are as follows:
     *
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
     * <td>NO</td>
     * <td>empty, 1970-2099</td>
     * <td>* , - /</td>
     * </tr>
     * </table>
     *
     * <p>Thus in general Quartz cron expressions are as follows:
     *
     * <p>S M H DoM M DoW [Y]
     *
     * @return {@link CronDefinition} instance, never {@code null}
     */
    private static CronDefinition quartz() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().withValidRange(0, 59).and()
                .withMinutes().withValidRange(0, 59).and()
                .withHours().withValidRange(0, 23).and()
                .withDayOfMonth().withValidRange(1, 31).supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().withValidRange(1, 12).and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).withStrictRange().optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }

    /**
     * Creates CronDefinition instance matching Spring (v5.2 and below) specification.
     *
     * <p>The cron expression is expected to be a string comprised of 6
     * fields separated by white space. Fields can contain any of the allowed
     * values, along with various combinations of the allowed special characters
     * for that field. The fields are as follows:
     *
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
     * <td>* ? , - /</td>
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
     * <td>0-7 or SUN-SAT</td>
     * <td>* ? , - /</td>
     * </tr>
     * </table>
     *
     * <p>Thus in general Spring cron expressions are as follows (up to version 5.2):
     *
     * <p>S M H DoM M DoW
     *
     * @return {@link CronDefinition} instance, never {@code null}
     */
    private static CronDefinition spring() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().withValidRange(0, 59).withStrictRange().and()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).supportsQuestionMark().and()
                .withMonth().withValidRange(1, 12).and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7,0)
                    .supportsHash().supportsQuestionMark().and()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching Spring (v5.2 onwards) specification.
     * https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions
     *
     * <p>The cron expression is expected to be a string comprised of 6
     * fields separated by white space. Fields can contain any of the allowed
     * values, along with various combinations of the allowed special characters
     * for that field. The fields are as follows:
     *
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
     * <td>0-7 or SUN-SAT</td>
     * <td>* ? , - / L #</td>
     * </tr>
     * </table>
     *
     * <p>Thus in general Spring cron expressions are as follows (from version 5.3 onwards):
     *
     * <p>S M H DoM M DoW
     *
     * @return {@link CronDefinition} instance, never {@code null}
     */
    private static CronDefinition spring53() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().withValidRange(0, 59).withStrictRange().and()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().withValidRange(1, 12).and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7,0)
                .supportsHash().supportsL().supportsQuestionMark().and()
                .withSupportedNicknameYearly().withSupportedNicknameAnnually()
                .withSupportedNicknameMonthly()
                .withSupportedNicknameWeekly()
                .withSupportedNicknameDaily().withSupportedNicknameMidnight()
                .withSupportedNicknameHourly()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching unix crontab specification.
     *
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition unixCrontab() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).withStrictRange().and()
                .withMonth().withValidRange(1, 12).withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).withStrictRange().and()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching cronType specification.
     *
     * @param cronType - some cron type. If null, a RuntimeException will be raised.
     * @return CronDefinition instance if definition is found; a RuntimeException otherwise.
     */
    public static CronDefinition instanceDefinitionFor(final CronType cronType) {
        switch (cronType) {
            case CRON4J:
                return cron4j();
            case QUARTZ:
                return quartz();
            case UNIX:
                return unixCrontab();
            case SPRING:
                return spring();
            case SPRING53:
                return spring53();
            default:
                throw new IllegalArgumentException(String.format("No cron definition found for %s", cronType));
        }
    }
}