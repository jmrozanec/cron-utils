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

package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

/**
 * Provide an example on how convert a cron expression to ISO8601
 */
@Ignore
public class Issue367Test {

	private CronParser parser;

	@Before
	public void setUp() {
		parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
	}

	@Test
	public void testCase1() throws ParseException {
		// set base data
		final ZoneId zone = ZoneId.of("Europe/Berlin");
		final Date date = new Date();
		final String cronExpression = "0 0 1 1 1 ?";
		// build cron
		Cron cron = parser.parse(cronExpression);
		// convert to quartz
		final org.quartz.CronExpression quartzExpression = new org.quartz.CronExpression(cron.asString());
		quartzExpression.setTimeZone(TimeZone.getTimeZone(zone));
		// get date and convert to ISO8601
		final Date quartzNextTime = quartzExpression.getNextValidTimeAfter(Date.from(date.toInstant()));// 2016-12-24T00:00:00Z
		ZonedDateTime d = ZonedDateTime.ofInstant(quartzNextTime.toInstant(), zone);
		String res = DateTimeFormatter.ISO_DATE_TIME.format(d);
		assertTrue(res.equals("2021-01-01T01:00:00+01:00[Europe/Berlin]"));
	}

}
