package com.cronutils.mapper;

public class ConstantsMapper {
    public static final WeekDay QUARTZ_WEEK_DAY = new WeekDay(2, false);
    public static final WeekDay JODATIME_WEEK_DAY = new WeekDay(1, false);
    public static final WeekDay CRONTAB_WEEK_DAY = new WeekDay(1, true);

    public static int weekDayMapping(WeekDay from, WeekDay to, int weekday){
        return from.map(to, weekday);
    }
}
