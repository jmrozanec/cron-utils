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
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * America/Sao_Paulo is only 3 hours behind UTC. Even with less difference, november 3 seems to be
 * ignored by timeFromLastExecution
 */
public class Issue404Test {

	@Ignore
	@Test
	public void testNovember3Midnight() {
		final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron().withMinutes().and().withHours().and()
			.withDayOfWeek().and().instance();

		final Cron cron = new CronParser(cronDefinition).parse("0 0 *");

		final ExecutionTime executionTime = ExecutionTime.forCron(cron);

		final ZonedDateTime time = ZonedDateTime.of(2019, 11, 3, 0, 0, 1, 0, ZoneId.of("America/Sao_Paulo"));

		final Duration timeFromLastExecution = executionTime.timeFromLastExecution(time).get();

		Assert.assertEquals(1, timeFromLastExecution.getSeconds());
	}

	@Ignore
	@Test
	public void testNovember3Noon() {
		final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron().withMinutes().and().withHours().and()
			.withDayOfWeek().and().instance();

		final Cron cron = new CronParser(cronDefinition).parse("0 0 *");

		final ExecutionTime executionTime = ExecutionTime.forCron(cron);

		final ZonedDateTime time = ZonedDateTime.of(2019, 11, 3, 12, 0, 1, 0, ZoneId.of("America/Sao_Paulo"));

		final Duration timeFromLastExecution = executionTime.timeFromLastExecution(time).get();

		Assert.assertEquals(12, timeFromLastExecution.toHours());
	}


	@Test
	public void testSaturdayMidnight() {
		final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron().withMinutes().and().withHours().and()
			.withDayOfWeek().and().instance();

		final Cron cron = new CronParser(cronDefinition).parse("0 0 *");

		final ExecutionTime executionTime = ExecutionTime.forCron(cron);

		final ZonedDateTime time = LocalDateTime.of(2019, 11, 2, 0, 0, 1).atZone(ZoneId.of("America/Sao_Paulo"));

		final Duration timeFromLastExecution = executionTime.timeFromLastExecution(time).get();

		Assert.assertEquals(1, timeFromLastExecution.getSeconds());
	}


	@Test
	public void testNoTimezone() {
		final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron().withMinutes().and().withHours().and()
			.withDayOfWeek().and().instance();

		final Cron cron = new CronParser(cronDefinition).parse("0 0 *");

		final ExecutionTime executionTime = ExecutionTime.forCron(cron);

		final ZonedDateTime time = LocalDateTime.of(2019, 11, 3, 16, 0, 0).atZone(ZoneId.of("UTC"));

		final Duration timeFromLastExecution = executionTime.timeFromLastExecution(time).get();

		Assert.assertEquals(16, timeFromLastExecution.toHours());
	}
}
