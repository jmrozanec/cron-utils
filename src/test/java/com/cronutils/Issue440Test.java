package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class Issue440Test {

	@Test
	public void testCase1() {
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
		String description = descriptor.describe(parser.parse("* 2,1/31 * * * ?"));

		assertEquals("every second at minute 2 and every 31 minutes", description);
	}

	@Test
	public void testCase2() {
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
		CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
		String description = descriptor.describe(parser.parse("2,1/31 * * * *"));

		assertEquals("at minute 2 and every 31 minutes", description);
	}
}
