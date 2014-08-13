cron-utils
===========

[![Stories in Backlog](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=backlog&title=Backlog)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in Ready](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=ready&title=Ready)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in In Progress](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=inprogress&title=InProgress)](http://waffle.io/jmrozanec/cron-utils)
[![Stories in Done](https://badge.waffle.io/jmrozanec/cron-utils.svg?label=done&title=Done)](http://waffle.io/jmrozanec/cron-utils)

[![Build Status](https://travis-ci.org/jmrozanec/cron-utils.png?branch=master)](https://travis-ci.org/jmrozanec/cron-utils)
[![Coverage Status](https://coveralls.io/repos/jmrozanec/cron-utils/badge.png)](https://coveralls.io/r/jmrozanec/cron-utils)
[ ![Download](https://api.bintray.com/packages/jmrozanec/cron-utils/cron-utils/images/download.png) ](https://bintray.com/jmrozanec/cron-utils/cron-utils/_latestVersion)

[![Project stats by OpenHub](https://www.openhub.net/p/cron-utils/widgets/project_thin_badge.gif)](https://www.openhub.net/p/cron-utils/)

A Java library to parse a cron and get a human readable description.

License: Apache 2.0

The project follows the [Semantic Versioning Convention](http://semver.org/)

**Features**

 * Supports all cron expression special characters including * / , - L W, #.
    * The question mark (?) is currently replaced for an asterisk (*). Enhanced support will be provided in a future.
 * Supports arbitrary cron expressions: you can define your own cron format! Supported fields are: second, minute, hour, day of month, month, day of week, year.
 * Support for optional last field!
 * Supports printing to locale specific human readable format (Italian, English, Spanish and Dutch so far...).
 * Parsing and Description process are decoupled: parse once and operate with the result!
 * Pre-defined parsers for the following cron definitions:
    * [Unix](http://www.unix.com/man-page/linux/5/crontab/)
    * [Cron4j](http://www.sauronsoftware.it/projects/cron4j/)
    * [Quartz](http://quartz-scheduler.org/)

**Download**

cron-utils will be soon available in the Maven central repository.

**Usage Examples**

    //define your own parser: arbitrary fields are allowed and last field can be optional
    CronParser parser =
        ParserDefinitionBuilder.defineParser()
            .withSeconds().and()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth()
                .supportsHash().supportsL().supportsW().and()
            .withMonth().and()
            .withDayOfWeek()
                .withIntMapping(7, 0).supportsHash().supportsL().supportsW().and()
            .withYear().and()
            .lastFieldOptional()
            .instance();

    //or get a predefined instance
    parser = CronParserRegistry.instance().retrieveParser(QUARTZ);

    //create a descriptor for a specific Locale
    CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

    //parse some expression and ask descriptor for description
    descriptor.describe(parser.parse("*/45 * * * * *"));
    //description will be: "every 45 seconds"

    descriptor.describe(parser.parse("0 23 ? * * 1-5 *"));
    //description will be: "every hour at minute 23 every day between Monday and Friday"
    //which is the same description we get for the cron below:
    descriptor.describe(parser.parse("0 23 ? * * MON-FRI *"));

**Contribute!**

Contributions are welcome! You can contribute by
 * adding new features
 * enhancing existing code: ex.: provide more accurate description cases
 * testing
 * enhancing documentation
 * providing properties files for new locales
 * bringing suggestions and reporting bugs
 * spreading the word / telling us how you use it!


For stats about the project, you can visit our [OpenHUB profile](https://www.openhub.net/p/cron-utils)
