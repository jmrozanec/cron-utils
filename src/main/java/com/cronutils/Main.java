package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

/**
 * Standalone calls to cron-utils
 *
 */
public class Main {

	private Main() {
		// No direct instantiation
		super();
	}

	/**
	 * Arguments :
	 * <li>arg1 : One of CronType enum name, example : 'UNIX'
	 * <li>arg2, arg3, ..., arg n : the cron expression
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 6) {
			throw new IllegalStateException("Not enough arguments\n"
					+ "Usage: java -jar cron-utils.jar com.cronutils.Main [CRON4J|QUARTZ|UNIX] <cron expression as several strings>\n"
					+ "Example: java -jar cron-utils.jar com.cronutils.Main UNIX * 1 * * *");
		}
		// Return an IllegalArgumentException if the provided argument is wrong,
		// this is fine
		CronType cronType = CronType.valueOf(args[0]);
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
		CronParser parser = new CronParser(cronDefinition);
		StringBuilder cronExpr = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			cronExpr.append(args[i]);
			cronExpr.append(' ');
		}
		Cron quartzCron = parser.parse(cronExpr.toString());
		quartzCron.validate();
	}

}
