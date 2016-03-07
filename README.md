cron-utils
===========
A Java library to parse, validate, migrate crons as well as get human readable descriptions for them. The project follows the [Semantic Versioning Convention](http://semver.org/) and uses Apache 2.0 license.

[![Gitter Chat](http://img.shields.io/badge/chat-online-brightgreen.svg)](https://gitter.im/jmrozanec/cron-utils)
[![Build Status](https://travis-ci.org/jmrozanec/cron-utils.png?branch=master)](https://travis-ci.org/jmrozanec/cron-utils)
[![Coverage Status](https://coveralls.io/repos/jmrozanec/cron-utils/badge.png)](https://coveralls.io/r/jmrozanec/cron-utils)

[![Project stats by OpenHub](https://www.openhub.net/p/cron-utils/widgets/project_thin_badge.gif)](https://www.openhub.net/p/cron-utils/)

**Download**

cron-utils is available on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.cronutils%22) repository.

    <dependency>
        <groupId>com.cronutils</groupId>
        <artifactId>cron-utils</artifactId>
        <version>3.1.6</version>
    </dependency>


**Features**

 * Create arbitrary cron expressions: you can define your own cron format! Supported fields are: second, minute, hour, day of month, month, day of week, year.
 * You can flag last field as optional!
 * Supports all cron special characters: * / , -
    * Non-standard characters L, W, LW, '?' and # are supported as well!
 * Print to locale specific human readable format (English, German, Korean and Spanish are fully supported. Dutch, French, Italian and Portuguese have basic support).
 * Parse and Description process are decoupled: parse once and operate with the result!
 * Validate if cron string expressions match a cron definition using CronValidator
 * Convert crons between different cron definitions: if you need to migrate expressions, CronMapper may help you!
 * Pre-defined definitions for the following cron libraries are provided:
    * [Unix](http://www.unix.com/man-page/linux/5/crontab/)
    * [Cron4j](http://www.sauronsoftware.it/projects/cron4j/)
    * [Quartz](http://quartz-scheduler.org/)
 * Obtain last/next execution time as well as time from last execution/time to next execution.
 * Need to map constants between different cron/time libraries? Use ConstantsMapper.

**Usage Examples**

***Build cron definitions***

    //define your own cron: arbitrary fields are allowed and last field can be optional
    CronDefinition cronDefinition =
        CronDefinitionBuilder.define()
            .withSeconds().and()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth()
                .supportsHash().supportsL().supportsW().and()
            .withMonth().and()
            .withDayOfWeek()
                .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
                .supportsHash().supportsL().supportsW().and()
            .withYear().and()
            .lastFieldOptional()
            .instance();

    //or get a predefined instance
    cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

***Parse***

    //create a parser based on provided definition
    CronParser parser = new CronParser(cronDefinition);
    Cron quartzCron = parser.parse("0 23 ? * * 1-5 *");

***Describe***

    //create a descriptor for a specific Locale
    CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

    //parse some expression and ask descriptor for description
    String description = descriptor.describe(parser.parse("*/45 * * * * *"));
    //description will be: "every 45 seconds"

    description = descriptor.describe(quartzCron);
    //description will be: "every hour at minute 23 every day between Monday and Friday"
    //which is the same description we get for the cron below:
    descriptor.describe(parser.parse("0 23 ? * * MON-FRI *"));

***Migrate***

    //Migration between cron libraries is easy!
    //Turn cron expressions into another format by using CronMapper:
    CronMapper cronMapper =
            new CronMapper(
                    cronDefinition,
                    CronDefinitionBuilder.instanceDefinitionFor(CRON4J)
            );
    Cron cron4jCron = cronMapper.map(quartzCron);
    //and to get a String representation of it, we can use
    cron4jCron.asString();//will return: 23 * * * 1-5

***Validate***

    //Validate if a string expression matches a cron definition:
    CronValidator quartzValidator = new CronValidator(cronDefinition);

    //getting a boolean result:
    quartzValidator.isValid("0 23 ? * * MON-FRI *");

    //or returning same string if valid and raising an exception if invalid
    quartzValidator.validate("0 23 ? * * MON-FRI *");

***Calculate time from/to execution***

    //Get date for last execution
    DateTime now = DateTime.now();
    ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* * * * * * *"));
    DateTime lastExecution = executionTime.lastExecution(now));

    //Get date for next execution
    DateTime nextExecution = executionTime.nextExecution(now));

    //Time from last execution
    Duration timeFromLastExecution = executionTime.timeFromLastExecution(now);

    //Time to next execution
    Duration timeToNextExecution = executionTime.timeToNextExecution(now);

***Map constants between libraries***

    //Map day of week value from Quartz to JodaTime
    int jodatimeDayOfWeek =
            ConstantsMapper.weekDayMapping(
                    ConstantsMapper.QUARTZ_WEEK_DAY,
                    ConstantsMapper.JODATIME_WEEK_DAY
            );

***Date and time formatting for humans!***

Use [htime](https://github.com/jmrozanec/htime) - Human readable datetime formatting for Java!
Despite this functionality is not bundled in the same jar, is a cron-utils project you may find useful.

    //You no longer need to remember "YYYY-MM-dd KK a" patterns.
    DateTimeFormatter formatter = 
	    HDateTimeFormatBuilder
		    .getInstance()
		    .forJodaTime()
		    .getFormatter(Locale.US)
		    .forPattern("June 9, 2011");
    String formattedDateTime = formatter.print(lastExecution);
    //formattedDateTime will be lastExecution in "dayOfWeek, Month day, Year" format


**Contribute & Support!**

Contributions are welcome! You can contribute by
 * star and/or Flattr this repo!
 * requesting or adding new features. Check our [roadmap](https://github.com/jmrozanec/cron-utils/wiki/Roadmap)!
 * enhancing existing code: ex.: provide more accurate description cases
 * testing
 * enhancing documentation
 * providing translations to support new locales
 * bringing suggestions and reporting bugs
 * spreading the word / telling us how you use it!


Check [our page](http://cronutils.com)! For stats about the project, you can visit our [OpenHUB profile](https://www.openhub.net/p/cron-utils).

Support us donating once or by subscription through Flattr!

[![Flattr this!](https://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=jmrozanec&url=https://github.com/jmrozanec/cron-utils)
