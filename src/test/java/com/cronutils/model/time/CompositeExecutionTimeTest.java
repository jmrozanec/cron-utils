package com.cronutils.model.time;

import static com.cronutils.model.CronType.QUARTZ;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
/**
 * Test for CompositeExecutionTime.java
 */
class CompositeExecutionTimeTest {

	CompositeExecutionTime compositeExecutionTime;
	CronParser parser;
	
	@BeforeEach
	public void setUp() {
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
		List<ExecutionTime> executionTimes = new ArrayList<ExecutionTime>();
		ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("4 * * * * ? *"));
		executionTimes.add(executionTime);
		executionTime = ExecutionTime.forCron(parser.parse("6 0 0 * * ? *"));
		executionTimes.add(executionTime);
		compositeExecutionTime = new CompositeExecutionTime(executionTimes);
	}
	
	/**
	 * Test lastExecution() method
	 */
	@Test
	void testLastExecution() {
		ZonedDateTime dateTime = ZonedDateTime.of(2022, 10, 15, 3, 2, 4, 2, UTC);
		Optional<ZonedDateTime> lastExecutionOptional = compositeExecutionTime.lastExecution(dateTime);
		if(lastExecutionOptional.isPresent()) {
			ZonedDateTime lastExecution = lastExecutionOptional.get();
			assertEquals(dateTime.getYear(), lastExecution.getYear());
			assertEquals(dateTime.getMonth(), lastExecution.getMonth());
			assertEquals(dateTime.getDayOfMonth(), lastExecution.getDayOfMonth());
			assertEquals(dateTime.getDayOfWeek(), lastExecution.getDayOfWeek());
			assertEquals(dateTime.getHour(), lastExecution.getHour());
			assertEquals(dateTime.getMinute(), lastExecution.getMinute());
			assertEquals(dateTime.getSecond(), lastExecution.getSecond());
			assertEquals(dateTime.getZone(), lastExecution.getZone());
		}
	}
}
