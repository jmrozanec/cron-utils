package com.cronutils.mapper;

public class ConstantsMapper {
    public static final WeekDay QUARTZ_WEEK_DAY = new WeekDay(2, false);
    public static final WeekDay JODATIME_WEEK_DAY = new WeekDay(1, false);
    public static final WeekDay CRONTAB_WEEK_DAY = new WeekDay(1, true);

    /**
     * Performs weekday mapping between two weekday definitions.
     * @param source - source
     * @param target - target weekday definition
     * @param weekday - value in source range.
     * @return
     */
    public static int weekDayMapping(WeekDay source, WeekDay target, int weekday){
        return source.mapTo(weekday, target);
    }
}
