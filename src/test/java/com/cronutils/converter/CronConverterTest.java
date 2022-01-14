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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.spy;

@RunWith(Parameterized.class)
public class CronConverterTest {

	private String timezone;
	private String inputCronExpression;
	private String expectedCronExpression;
	private CronConverter cronConverter = spy(new CronConverter(
			new CronToCalendarTransformer(),
			new CalendarToCronTransformer()
	));

	public CronConverterTest(String timezone, String inputCronExpression, String expectedCronExpression) {
		this.timezone = timezone;
		this.inputCronExpression = inputCronExpression;
		this.expectedCronExpression = expectedCronExpression;
	}

	@Parameterized.Parameters
	public static Collection cronExpressions() {
		return Arrays.asList(new Object[][] { { "Pacific/Pago_Pago", "15 * * * *", "15 * * * *" },
				{ "Antarctica/Casey", "? * * * *", "? * * * *" }, { "Antarctica/Troll", "45 * * * *", "45 * * * *" },
				{ "Pacific/Chatham", "15 * * * *", "30 * * * *" }, { "Asia/Colombo", "45 * * ? *", "15 * * ? *" },
				{ "Asia/Colombo", "0/45 * * ? *", "0/45 * * ? *" }, { "Australia/Eucla", "13 * * ? *", "28 * * ? *" },
				{ "America/St_Johns", "0 0/15 * * * ?", "30 0/15 * * * ?" },
				{ "America/St_Johns", "0 8 * * ?", "30 10 * * ?" },
				{ "America/St_Johns", "0 0/1 * * ?", "30 0/1 * * ?" },
				{ "America/St_Johns", "20 0 * * ?", "50 2 * * ?" }, { "Asia/Kolkata", "20 0 * * ?", "50 18 * * ?" }, });
	}

	@Test
	@Ignore //TODO: fix
	public void testCronConverterBuilder() {
		Assert.assertEquals(expectedCronExpression,
				cronConverter.using(inputCronExpression)
						.from(ZoneId.of(timezone)).to(ZoneId.of("UTC"))
						.convert());
	}
}
