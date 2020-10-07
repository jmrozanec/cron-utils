package com.cronutils;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

public class Issue439Test {

	private CronParser parser;

	@Before
	public void setUp() {
		parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
	}

	@Test
	public void test() {
		CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
		String description = descriptor.describe(parser.parse("* 0 * * * ?"));
		assertTrue(description.equals("every second at minute 00 of every hour"));
	}
}
