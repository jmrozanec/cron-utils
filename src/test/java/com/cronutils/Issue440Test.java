package com.cronutils;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

public class Issue440Test {

	private CronParser parser;

	@Before
	public void setUp() {
		parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
	}

	@Test
	public void testCase1() {
		CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
		String description = descriptor.describe(parser.parse("* 2,1/31 * * * ?"));
		assertTrue(description.equalsIgnoreCase("Every second at 2 minutes and every 31 minutes"));
	}
}
