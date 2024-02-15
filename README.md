# cron-utils
_We define crons. And support them._


cron-utils is a Java library to define, parse, validate, migrate crons as well as get human readable descriptions for them. The project follows the [Semantic Versioning Convention](http://semver.org/), provides OSGi metadata and uses Apache 2.0 license.

[![Gitter Chat](http://img.shields.io/badge/chat-online-brightgreen.svg)](https://gitter.im/jmrozanec/cron-utils)
[![Build Status](https://travis-ci.org/jmrozanec/cron-utils.png?branch=master)](https://travis-ci.org/jmrozanec/cron-utils)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=cron-utils&metric=coverage)](https://sonarcloud.io/dashboard/index/cron-utils)


**Download**

cron-utils is available on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.cronutils%22) repository.

    <dependency>
        <groupId>com.cronutils</groupId>
        <artifactId>cron-utils</artifactId>
        <version>9.2.0</version>
    </dependency>

For Android developers, cron-utils 7.0.0 assumes Android 26+. For earlier Android versions consider using cron-utils 6.0.6.
If using ScheduleExpression from Java EE or Jakarta, this should be provided as a runtime dependency.

**Current development**

Now we are developing a new generation of cron-descriptors using neural-translation! Any kind of contributions are welcome: from help with dataset generation to machine learning models training and utilities to load them! If interested, please follow issue [#3](https://github.com/jmrozanec/cron-utils/issues/3)

**Features**

 * Create arbitrary cron expressions: you can define your own cron format! Supported fields are: second, minute, hour, day of month, month, day of week, year.
 * You can flag last field as optional!
 * Supports all cron special characters: * / , -
    * Non-standard characters L, W, LW, '?' and # are supported as well!
 * Supports cron extensions / nicknames: @yearly, @annually, @monthly, @weekly, @daily, @midnight, @hourly, @reboot
 * Print to locale specific human readable format (Chinese, English, German, Greek, Indonesian, Japanese, Korean, Polish, Romanian, Spanish, Swahili, and Turkish are fully supported. Dutch, French, Italian, Portuguese and Russian have basic support).
 * Parse and Description process are decoupled: parse once and operate with the result!
 * Build cron expressions using CronBuilder: 
    * no need to remember fields and constraints for each cron provider.
    * crons become decoupled from cron provider: anytime you can export to another format.
 * Check if cron expressions are equivalent.
 * Squash multiple cron expressions into a single one!
 * Validate if cron string expressions match a cron definition.
 * Convert crons between different cron definitions: if you need to migrate expressions, CronMapper may help you!
 * Pre-defined definitions for the following cron libraries are provided:
    * [Unix](http://www.unix.com/man-page/linux/5/crontab/)
    * [Cron4j](http://www.sauronsoftware.it/projects/cron4j/)
    * [Quartz](http://quartz-scheduler.org/)
    * [Spring](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html). We support two definitions: prior to v5.3 and [from v5.3 onwards](https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions).
 * Obtain last/next execution time as well as time from last execution/time to next execution.
 * Compute the number of executions between two days, and compare crons based on their execution frequencies.
 * Obtain weekdays count between two dates, considering different weekend policies as well as holidays.
 * Need to map constants between different cron/time libraries? Use ConstantsMapper.

**Usage Examples**

Below we present some examples. You can find this and others in a [sample repo we created](https://github.com/jmrozanec/cron-utils-examples) to showcase cron-utils libraries!

***Build cron definitions***

```java
// Define your own cron: arbitrary fields are allowed and last field can be optional
CronDefinition cronDefinition =
    CronDefinitionBuilder.defineCron()
        .withSeconds().and()
        .withMinutes().and()
        .withHours().and()
        .withDayOfMonth()
            .supportsHash().supportsL().supportsW().and()
        .withMonth().and()
        .withDayOfWeek()
            .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
            .supportsHash().supportsL().supportsW().and()
        .withYear().optional().and()
        .instance();

// or get a predefined instance
cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
```

***Build a cron expression***
```java 
// Create a cron expression. CronMigrator will ensure you remain cron provider agnostic
import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
    .withYear(always())
    .withDoM(between(SpecialChar.L, 3))
    .withMonth(always())
    .withDoW(questionMark())
    .withHour(always())
    .withMinute(always())
    .withSecond(on(0))
    .instance();
// Obtain the string expression
String cronAsString = cron.asString(); // 0 * * L-3 * ? *
```

***Parse***
```java
// Create a parser based on provided definition
CronParser parser = new CronParser(cronDefinition);
Cron quartzCron = parser.parse("0 23 * ? * 1-5 *");
```

... even multi-cron expressions! How about squashing multiple crons into a single line? Instead of writing ```0 0 9 * * ? *```, ```0 0 10 * * ? *```, ```0 30 11 * * ? *``` and ```0 0 12 * * ? *``` we can wrap it into ```0 0|0|30|0 9|10|11|12 * * ? *```


***Describe***
```java
// Create a descriptor for a specific Locale
CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

// Parse some expression and ask descriptor for description
String description = descriptor.describe(parser.parse("*/45 * * * * ?"));
// Description will be: "every 45 seconds"

description = descriptor.describe(quartzCron);
// Description will be: "every hour at minute 23 every day between Monday and Friday"
// which is the same description we get for the cron below:
descriptor.describe(parser.parse("0 23 * ? * MON-FRI *"));
```

***Migrate***
```java
// Migration between cron libraries has never been so easy!
// Turn cron expressions into another format by using CronMapper:
CronMapper cronMapper = CronMapper.fromQuartzToCron4j();

Cron cron4jCron = cronMapper.map(quartzCron);
// and to get a String representation of it, we can use
cron4jCron.asString();//will return: 23 * * * 1-5
```

***Validate***
```java
cron4jCron.validate()
```

***Calculate time from/to execution***
```java
// Get date for last execution
ZonedDateTime now = ZonedDateTime.now();
ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* * * * * ? *"));
ZonedDateTime lastExecution = executionTime.lastExecution(now);

// Get date for next execution
ZonedDateTime nextExecution = executionTime.nextExecution(now);

// Time from last execution
Duration timeFromLastExecution = executionTime.timeFromLastExecution(now);

// Time to next execution
Duration timeToNextExecution = executionTime.timeToNextExecution(now);
```

***Map constants between libraries***
```java
// Map day of week value from Quartz to JodaTime
int jodatimeDayOfWeek =
        ConstantsMapper.weekDayMapping(
                ConstantsMapper.QUARTZ_WEEK_DAY,
                ConstantsMapper.JODATIME_WEEK_DAY
        );
```
***Date and time formatting for humans!***

Use [htime](https://github.com/jmrozanec/htime) - Human readable datetime formatting for Java!
Despite this functionality is not bundled in the same jar, is a cron-utils project you may find useful.
```java
// You no longer need to remember "YYYY-MM-dd KK a" patterns.
DateTimeFormatter formatter = 
	    HDateTimeFormatBuilder
		    .getInstance()
		    .forJodaTime()
		    .getFormatter(Locale.US)
		    .forPattern("June 9, 2011");
String formattedDateTime = formatter.print(lastExecution);
// formattedDateTime will be lastExecution in "dayOfWeek, Month day, Year" format
```

***cron-utils CLI***

We provide a simple CLI interface to use cron-utils right from console, without writing a new project! The CLI is a satellite project, available at [cron-utils-cli](https://github.com/jmrozanec/cron-utils-cli)

- Usage: `java -jar cron-utils.jar com.cronutils.cli.CronUtilsCLI --validate -f [CRON4J|QUARTZ|UNIX] -e '<cron expression>'`

- Example: `java -jar cron-utils.jar com.cronutils.cli.CronUtilsCLI --validate -f UNIX -e '* 1 * * *'`

If you want a standalone jar without requiring the 'cp', build an uber jar with :
```bash
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```
Then, launch cli-utils (built in the `target` directory) with :
```bash
java -jar cron-utils-<version>-jar-with-dependencies.jar com.cronutils.cli.CronUtilsCLI --validate -f [CRON4J|QUARTZ|UNIX] -e '<cron expression>'`
```

**Contribute & Support!**

Contributions are welcome! You can contribute by
 * starring this repo!
 * requesting or adding new features. Check our [roadmap](https://github.com/jmrozanec/cron-utils/wiki/Roadmap)!
 * enhancing existing code: ex.: provide more accurate description cases
 * testing
 * enhancing documentation
 * providing translations to support new locales
 * bringing suggestions and reporting bugs
 * spreading the word 
 * telling us how you use it! We look forward to [list you at our wiki](https://github.com/jmrozanec/cron-utils/wiki/Projects-using-cron-utils)!


Check [our page](http://cronutils.com)! For stats about the project, you can visit our [OpenHUB profile](https://www.openhub.net/p/cron-utils).

**Other cron-utils projects**

You are welcome to visit and use the following cron-utils projects:
 * [htime](https://github.com/jmrozanec/htime): A Java library to make it easy for humans format a date. You no longer need to remember date time formatting chars: just write an example, and you will get the appropriate formatter.
 * [cron-utils-spring](https://github.com/jmrozanec/cron-utils-spring): A Java library to describe cron expressions in human readable language at Spring framework, using cron-utils.
 * [cron-utils-cli](https://github.com/jmrozanec/cron-utils-cli): cron-utils features made available through a CLI.
 * [cron-utils-sisyphus](https://github.com/jmrozanec/cron-utils-sisyphus): A Scala scheduler that supports multiple cron notations.
 * [cron-utils-scheduler](https://github.com/jmrozanec/cron-utils-scheduler): A Java job scheduler based on cron-utils library.
