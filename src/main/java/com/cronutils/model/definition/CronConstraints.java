package com.cronutils.model.definition;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.QuestionMark;

final class CronConstraints {
    
    private CronConstraints() {/*HIDE*/}

    /**
     * Creates CronConstraint to ensure that either day-of-year or month is assigned a specific value.
     * @return newly created CronConstraint instance, never {@code null};
     */
    static CronConstraint ensureEitherDayOfYearOrMonth() {
        return new CronConstraint("Both, a day-of-year AND a day-of-month or day-of-week, are not supported.") {

            private static final long serialVersionUID = 520379111876897579L;

            @Override
            public boolean validate(Cron cron) {
                CronField dayOfYearField = cron.retrieve(CronFieldName.DAY_OF_YEAR);
                if (dayOfYearField != null && !(dayOfYearField.getExpression() instanceof QuestionMark)) {
                    return cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark
                        && cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark;
                }
                
                return true;
            }
         };
    }
      
    static CronConstraint ensureEitherDayOfWeekOrDayOfMonth() {
        //Solves issue #63: https://github.com/jmrozanec/cron-utils/issues/63
        //both a day-of-week AND a day-of-month parameter should fail for QUARTZ
        return new CronConstraint("Both, a day-of-week AND a day-of-month parameter, are not supported.") {

            private static final long serialVersionUID = -4423693913868081656L;

            @Override
            public boolean validate(Cron cron) {
                CronField dayOfYearField = cron.retrieve(CronFieldName.DAY_OF_YEAR);
                if (dayOfYearField == null || dayOfYearField.getExpression() instanceof QuestionMark) {
                    if(!(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark)){
                        return cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark;
                    } else {
                        return !(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark);
                    }
                }
                
                return true;
            }
        };
    }
}
