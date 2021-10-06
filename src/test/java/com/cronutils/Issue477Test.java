package com.cronutils;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

public class Issue477Test {

	@Test
	public void testDescribe() {
		CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
		CronDefinition cd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
		CronParser parser = new CronParser(cd);

		assertEquals("every 2 minutes every 2 hours", descriptor.describe(parser.parse("*/2 */2 * * *")));
		assertEquals("every minute every 2 hours", descriptor.describe(parser.parse("* */2 * * *")));
		assertEquals("every minute every 2 hours", descriptor.describe(parser.parse("*/1 */2 * * *")));
	}

}
