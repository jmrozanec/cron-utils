package com.cronutils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class Issue470Test {

	@Test
	public void testWithoutYearInPattern() {
		ZonedDateTime dt = ZonedDateTime.of(2021, 3, 23, 0, 0, 0, 0, ZoneId.of("UTC"));
		CronDefinition cd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
		CronParser parser = new CronParser(cd);
		Cron cron = parser.parse("0 0 0 23 3 ? *");
		cron.validate();
		System.out.println(cron.asString());
	    ExecutionTime et1 = ExecutionTime.forCron(cron);
	    Optional<ZonedDateTime> next = et1.nextExecution(dt);
	    Optional<ZonedDateTime> last = et1.lastExecution(dt);
	    System.out.println("Next: " + next + " Last: " + last);
	    assert(next.isPresent());	// Without year in pattern, there is always a next execution date
	    assert(last.isPresent());	// Without year in pattern, there is always a previous execution date
	    assert(et1.isMatch(dt));	// And, we also match this year
	}

	@Test
	public void testWithYearInPattern() {
		ZonedDateTime dt = ZonedDateTime.of(2021, 3, 23, 0, 0, 0, 0, ZoneId.of("UTC"));
		CronDefinition cd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
		CronParser parser = new CronParser(cd);
		Cron cron = parser.parse("0 0 0 23 3 ? 2021");
		cron.validate();
		System.out.println(cron.asString());
	    ExecutionTime et1 = ExecutionTime.forCron(cron);
	    Optional<ZonedDateTime> next = et1.nextExecution(dt);
	    Optional<ZonedDateTime> last = et1.lastExecution(dt);
	    System.out.println("Next: " + next + " Last: " + last);
	    assert(!next.isPresent());	// We only should match once, on this exact date
	    assert(!last.isPresent());	// therefore, next and last should be (and are) empty
	    assert(et1.isMatch(dt));// Fails in 9.1.3
	}

}
