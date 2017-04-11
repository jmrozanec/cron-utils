package com.cronutils.cli;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.apache.commons.cli.*;

/*
 * Copyright 2017 bflorat, jmrozanec
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
public class CronUtilsCLI {
	private CronUtilsCLI() {}

	public static void main(String[] args) throws Exception {
		cronValidation(args);
	}

	/**
	 * Arguments:
	 * <li>f: cron format as CronType enum name. Example: 'UNIX', 'QUARTZ' or 'CRON4J'
	 * <li>e: the cron expression. Example: '* 1 * * *'
	 * <li>h: help
	 *
	 * @param args
	 */
	private static void cronValidation(String[] args) throws ParseException {
		Options options = new Options();
		options.addRequiredOption("a", "action",true, "Action to be performed. Possible values are: 'validation'");
		options.addOption("f", "format",true, "Cron expression format to validate. Possible values are: CRON4J, QUARTZ, UNIX");
		options.addOption("e", "expression",true, "Cron expression to validate. Example: '* 1 * * *'");
		options.addOption("h", "help", false, "Help");

		String header = "Cron expressions validation by cron-utils\n\n";
		String footer = "\nPlease report issues at https://github.com/jmrozanec/cron-utils/issues";

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("cron-validator", header, options, footer, true);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		if(cmd.getOptionValue("a").equalsIgnoreCase("validation")){
			String format = cmd.getOptionValue("f");
			String expression = cmd.getOptionValue("e");

			CronType cronType = CronType.valueOf(format);
			CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
			CronParser cronParser = new CronParser(cronDefinition);
			Cron quartzCron = cronParser.parse(expression);
			quartzCron.validate();
		}
	}
}
