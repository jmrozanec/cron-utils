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
                .withMonth()/*TODO/*.supportsQuestionMark()*/.and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).optional().and()
                .withDayOfYear().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and()
/*                
                .withCronValidation(
                        //Solves issue #63: https://github.com/jmrozanec/cron-utils/issues/63
                        //both a day-of-week AND a day-of-month parameter should fail for QUARTZ
                        new CronConstraint("Both, a day-of-week AND a day-of-month parameter, are not supported.") {
                            @Override
                            public boolean validate(Cron cron) {
                                if(!(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark)){
                                    return cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark;
                                } else {
                                    return !(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark);
                                }
                            }
                        })
*/
                .instance();
    }

}
