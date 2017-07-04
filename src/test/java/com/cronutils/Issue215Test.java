/**
 * 
 */
package com.cronutils;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Optional;

public class Issue215Test {

    // test für https://github.com/jmrozanec/cron-utils/issues/215
    @Test
    public void testWorkdayBugWithNextMonth() {

        testWorkdays8( LocalDateTime.of( 2017, 7, 7, 10, 00 ), LocalDateTime.of( 2017, 7, 10, 8, 00 ) ); //good
        testWorkdays8( LocalDateTime.of( 2017, 8, 31, 10, 00 ), LocalDateTime.of( 2017, 9, 1, 8, 00 ) ); //good
        testWorkdays8( LocalDateTime.of( 2017, 6, 30, 10, 00 ), LocalDateTime.of( 2017, 7, 3, 8, 00 ) ); //not good
        testWorkdays8( LocalDateTime.of( 2017, 9, 29, 10, 00 ), LocalDateTime.of( 2017, 10, 2, 8, 00 ) ); //not good
    }

    private void testWorkdays8( LocalDateTime startDate, LocalDateTime expectedNextExecution ) {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor( CronType.QUARTZ ) );
        Cron quartzCron = parser.parse( "0 0 8 ? * MON-FRI" );
        ExecutionTime executionTime = ExecutionTime.forCron( quartzCron );
        ZonedDateTime zonedDateTime = ZonedDateTime.of( startDate, ZoneId.systemDefault() );
        org.threeten.bp.ZonedDateTime xx = timeToThreeten( zonedDateTime );
        Optional<org.threeten.bp.ZonedDateTime> next = executionTime.nextExecution( xx );
        assertEquals( timeToThreeten( ZonedDateTime.of( expectedNextExecution, ZoneId.systemDefault() ) ), next.get() );
    }

    private org.threeten.bp.ZonedDateTime timeToThreeten( ZonedDateTime zonedDateTime ) {
        org.threeten.bp.ZonedDateTime xx =
                        org.threeten.bp.ZonedDateTime.of( zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(), zonedDateTime.getSecond(), zonedDateTime.getNano(), org.threeten.bp.ZoneId.of( zonedDateTime.getZone().getId() ) );
        return xx;
    }

}
