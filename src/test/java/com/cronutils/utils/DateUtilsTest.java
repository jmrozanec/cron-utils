package com.cronutils.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateUtilsTest {

    //@Test
    public void workdaysCountPolicyThursday() throws Exception {
        LocalDate date = LocalDate.of(2018, 1, 6);//this is a saturday
        int daysToEndDate = 1;
        WeekendPolicy policy = WeekendPolicy.THURSDAY_FRIDAY;
        int daysToWorkday = DateUtils.workdaysCount(ZonedDateTime.of(date, LocalTime.of(1, 0), ZoneId.of("America/Argentina/Buenos_Aires")), daysToEndDate, new ArrayList<>(), policy);
        assertEquals(1, daysToWorkday);
    }

    //@Test
    public void workdaysCountPolicyFriday() throws Exception {
        LocalDate date = LocalDate.of(2018, 1, 6);//this is a saturday
        int daysToEndDate = 1;
        WeekendPolicy policy = WeekendPolicy.FRIDAY_SATURDAY;
        int daysToWorkday = DateUtils.workdaysCount(ZonedDateTime.of(date, LocalTime.of(1, 0), ZoneId.of("America/Argentina/Buenos_Aires")), daysToEndDate, new ArrayList<>(), policy);
        assertEquals(0, daysToWorkday);
    }

    //@Test
    public void workdaysCountPolicySaturday() throws Exception {
        LocalDate date = LocalDate.of(2018, 1, 6);//this is a saturday
        int daysToEndDate = 1;
        WeekendPolicy policy = WeekendPolicy.SATURDAY_SUNDAY;
        int daysToWorkday = DateUtils.workdaysCount(ZonedDateTime.of(date, LocalTime.of(1, 0), ZoneId.of("America/Argentina/Buenos_Aires")), daysToEndDate, new ArrayList<>(), policy);
        assertEquals(0, daysToWorkday);
    }
}