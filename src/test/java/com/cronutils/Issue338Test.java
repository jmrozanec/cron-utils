package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue338Test {

	@Test
	public void testEverySecondInFrench() {
		CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		String cronString = "* * * * * ? *";
		Cron cron = cronParser.parse(cronString);
		String description = CronDescriptor.instance(Locale.FRANCE).describe(cron);
		assertEquals("chaque seconde", description);
	}
}
