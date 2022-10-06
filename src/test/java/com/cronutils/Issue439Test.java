package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue439Test {

	private CronParser parser;

	@BeforeEach
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
