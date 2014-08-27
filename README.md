cron-utils
===========
A Java library to parse, describe, migrate and validate crons.

[![Stories in Backlog](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=backlog&title=Backlog)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in Ready](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=ready&title=Ready)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in In Progress](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=inprogress&title=InProgress)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in Done](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=done&title=Done)](http://waffle.io/jmrozanec/cron-utils)

[![Build Status](https://travis-ci.org/jmrozanec/cron-utils.png?branch=master)](https://travis-ci.org/jmrozanec/cron-utils)
[![Coverage Status](https://coveralls.io/repos/jmrozanec/cron-utils/badge.png)](https://coveralls.io/r/jmrozanec/cron-utils)
[ ![Download](https://api.bintray.com/packages/jmrozanec/cron-utils/cron-utils/images/download.png) ](https://bintray.com/jmrozanec/cron-utils/cron-utils/_latestVersion)

[![Project stats by OpenHub](https://www.openhub.net/p/cron-utils/widgets/project_thin_badge.gif)](https://www.openhub.net/p/cron-utils/)

The project follows the [Semantic Versioning Convention](http://semver.org/)

License: Apache 2.0

**Features**

 * Create arbitrary cron expressions: you can define your own cron format! Supported fields are: second, minute, hour, day of month, month, day of week, year.
 * You can flag last field as optional!
 * Supports all cron special characters: * / , -
    * Non-standard characters L W, # are supported as well!
    * Question mark (?) is currently replaced for an asterisk (*). Enhanced support will be provided in a future.
 * Print to locale specific human readable format (English, Italian, Spanish and Dutch so far...).
 * Parse and Description process are decoupled: parse once and operate with the result!
 * Validate if cron string expressions match a cron definition using CronValidator
 * Convert crons between different cron definitions: if you need to migrate expressions, CronMapper may help you!
 * Pre-defined definitions for the following cron libraries are provided:
    * [Unix](http://www.unix.com/man-page/linux/5/crontab/)
    * [Cron4j](http://www.sauronsoftware.it/projects/cron4j/)
    * [Quartz](http://quartz-scheduler.org/)

**Download**

cron-utils will be soon available in the Maven central repository.

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



**Contribute!**

Contributions are welcome! You can contribute by
 * starring this repo!
 * adding new features
 * enhancing existing code: ex.: provide more accurate description cases
 * testing
 * enhancing documentation
 * providing translations to support new locales
 * bringing suggestions and reporting bugs
 * spreading the word / telling us how you use it!


For stats about the project, you can visit our [OpenHUB profile](https://www.openhub.net/p/cron-utils)
