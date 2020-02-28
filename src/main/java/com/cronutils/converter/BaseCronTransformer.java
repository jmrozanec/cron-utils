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

import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCronTransformer {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(BaseCronTransformer.class);

	protected String[] cronParts;

	protected Calendar calendarInstance;

	protected Integer calendarField;

	protected int cronFieldPosition;

	protected Integer cronFieldValue;

	Map<Integer, Integer> CRON_FIELDS_POSITION_MAP = Collections
			.unmodifiableMap(new HashMap<Integer, Integer>() {

				private static final long serialVersionUID = 911848294809282617L;
				{
					put(0, Calendar.MINUTE);
					put(1, Calendar.HOUR_OF_DAY);
					put(2, Calendar.DAY_OF_MONTH);
					put(3, Calendar.MONTH);
					put(4, Calendar.DAY_OF_WEEK);
				}
			});

	public void apply(String[] cronParts, Calendar calendarInstance) {
		this.cronParts = cronParts;
		this.calendarInstance = calendarInstance;
		for (int i = 0; i < cronParts.length; i++) {
			if (isNumeric(cronParts[i])) {
				calendarField = CRON_FIELDS_POSITION_MAP.get(i);
				cronFieldPosition = i;
				cronFieldValue = Integer.parseInt(cronParts[i]);
				transform();
			}
		}
	}

	protected abstract void transform();
}
