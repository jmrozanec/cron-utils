package com.cronutils.model.definition;

public final class CronDefinitions {
    
    private static final int LEAP_YEAR_DAY_COUNT = 366;
    
    private CronDefinitions() {/*HIDE*/}
    
    /**
     * Creates CronDefinition instance matching quartz specification;
     * @return CronDefinition instance, never null;
     */
    public static CronDefinition quartzWithDayOfYearExtension() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).optional().and()
                .withDayOfYear().supportsQuestionMark().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and()             
                .withCronValidation(CronConstraints.ensureEitherDayOfYearOrMonth())
                .withCronValidation(CronConstraints.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }

}
