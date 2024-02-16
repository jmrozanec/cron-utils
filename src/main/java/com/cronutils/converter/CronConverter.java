/*
 * Copyright 2019 fahmpeermoh
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

package com.cronutils.converter;

import com.cronutils.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.Function;

public class CronConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronConverter.class);
    private static final Function<ZoneId, Calendar> DEFAULT_CALENDAR_FACTORY = (zoneId) -> Calendar.getInstance(TimeZone.getTimeZone(zoneId));

    private static final String CRON_FIELDS_SEPARATOR = " ";
    private String[] cronParts;
    private Calendar fromCalendar;
    private String sourceCron;
    private ZoneId sourceZoneId;
    private ZoneId targetZoneId;

    private CronToCalendarTransformer toCalendarConverter;
    private CalendarToCronTransformer toCronConverter;

    private Function<ZoneId, Calendar> calendarFactory;

    public CronConverter(CronToCalendarTransformer toCalendarConverter, CalendarToCronTransformer toCronConverter) {
        this(toCalendarConverter, toCronConverter, DEFAULT_CALENDAR_FACTORY);
    }

    public CronConverter(CronToCalendarTransformer toCalendarConverter, CalendarToCronTransformer toCronConverter, Function<ZoneId, Calendar> calendarFactory) {
        this.toCalendarConverter = toCalendarConverter;
        this.toCronConverter = toCronConverter;
        this.calendarFactory = calendarFactory;
    }

    public CronConverter using(String cronExpression) {
        this.sourceCron = cronExpression;
        cronParts = cronExpression.split(CRON_FIELDS_SEPARATOR);
        LOGGER.debug("Cron '{}' split into {}", cronExpression, cronParts);
        return this;
    }

    public CronConverter from(ZoneId zoneId) {
        sourceZoneId = zoneId;
        fromCalendar = calendarFactory.apply(zoneId);
        toCalendarConverter.apply(cronParts, fromCalendar);
        LOGGER.debug("Calendar object built using cron :{} and zoneID {} => {}",
                cronParts, zoneId, fromCalendar);
        return this;
    }

    public CronConverter to(ZoneId zoneId) {
        targetZoneId = zoneId;
        Calendar toCalendar = calendarFactory.apply(zoneId);
        toCalendar.setTimeInMillis(fromCalendar.getTimeInMillis());
        LOGGER.debug(
                "Calendar object built from calendar {} and zoneID {} => {}",
                fromCalendar, zoneId, toCalendar);
        toCronConverter.apply(cronParts, toCalendar);
        LOGGER.debug("cron after applying calendar {} => {}", toCalendar,
                cronParts);
        return this;
    }

    public String convert() {
        String targetCron = StringUtils.join(cronParts, CRON_FIELDS_SEPARATOR);
        LOGGER.info("Converted CRON -- {} :[{}] => {} :[{}]", sourceZoneId,
                sourceCron, targetZoneId, targetCron);
        return targetCron;
    }
}
