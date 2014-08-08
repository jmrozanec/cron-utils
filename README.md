cron-utils
===========

[![Build Status](https://travis-ci.org/jmrozanec/cron-utils.png?branch=master)](https://travis-ci.org/jmrozanec/cron-utils)

A Java library for cron parsing and describe parsed expressions in a human readable language

License: Apache v 2.0
The library follows the [Semantic Versioning Convention](http://semver.org/)

**Features**

 * Supports all cron expression special characters including * / , - ? L W, #.
 * Supports arbitrary cron expressions: you can define your own cron format! Supported fields are: second, minute, hour, day of month, month, day of week, year.
 * Support for optional last field!
 * Supports printing to locale specific human readable format (Italian, English, Spanish and Dutch so far...).
 * Parsing and Description process are decoupled: parse once and operate with the result!
 * Pre-defined parsers for
    * [Unix](http://www.unix.com/man-page/linux/5/crontab/)
    * [Cron4j](http://www.sauronsoftware.it/projects/cron4j/)
    * [Quartz](http://quartz-scheduler.org/) cron definitions

**Download**

cron-utils will be soon available in the Maven central repository.

**Usage Examples**

***Quick example***

    //define your own parser: arbitrary fields are allowed and last field can be optional
    CronParser parser = ParserDefinitionBuilder.defineParser()
                    .withSeconds()
                    .withMinutes()
                    .withHours()
                    .withDayOfMonth()
                    .withMonth()
                    .withDayOfWeek()
                    .withYear()
                    .andLastFieldOptional()
                    .instance();

    //or get a predefined instance
    parser = CronParserRegistry.instance().retrieveParser(QUARTZ);

    //create a descriptor for a specific Locale
    CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

    //parse some expression and ask descriptor for description
    descriptor.describe(parser.parse("*/45 * * * * *")); //description will be: "Every 45 seconds"

    descriptor.describe(parser.parse("0 23 ? * MON-FRI *")); //description will be: "Every minute 23 every day between Monday and Friday"
