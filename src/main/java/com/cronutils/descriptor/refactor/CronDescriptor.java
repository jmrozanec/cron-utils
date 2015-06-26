package com.cronutils.descriptor.refactor;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.google.common.collect.Range;
import org.apache.commons.lang3.Validate;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

class CronDescriptor {
    public static final Locale DEFAULT_LOCALE = Locale.UK;
    private static final String BUNDLE = "cron-descI18N";
    private ResourceBundle bundle;

    /**
     * Constructor creating a descriptor for given Locale
     * @param locale - Locale in which descriptions are given
     */
    private CronDescriptor(Locale locale) {
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    /**
     * Default constructor. Considers Locale.UK as default locale
     */
    private CronDescriptor() {
        bundle = ResourceBundle.getBundle(BUNDLE, DEFAULT_LOCALE);
    }

    /**
     * Provide a description of given CronFieldParseResult list
     * @param cron - Cron instance, never null
     *             if null, will throw NullPointerException
     * @return description - String
     */
    public String describe(Cron cron) {
        Validate.notNull(cron, "Cron must not be null");
        Map<CronFieldName, CronField> expressions = cron.retrieveFieldsAsMap();
        FieldDescriptor descriptor = new FieldDescriptor(bundle);
        StringBuilder builder = new StringBuilder();

        CronField year;
        Range<Integer> yearRange;
        if(expressions.containsKey(CronFieldName.YEAR)){
            year = expressions.get(CronFieldName.YEAR);
            yearRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.YEAR).getConstraints().getValidRange();
        } else {
            year = new CronField(CronFieldName.YEAR, new Always(FieldConstraintsBuilder.instance().createConstraintsInstance()));
            yearRange = Range.all();
        }
        CronField month = expressions.get(CronFieldName.MONTH);
        Range<Integer> monthRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.MONTH).getConstraints().getValidRange();
        CronField dom = expressions.get(CronFieldName.DAY_OF_MONTH);
        Range<Integer> domRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.DAY_OF_MONTH).getConstraints().getValidRange();
        CronField dow = expressions.get(CronFieldName.DAY_OF_WEEK);
        Range<Integer> dowRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.DAY_OF_WEEK).getConstraints().getValidRange();
        CronField hour = expressions.get(CronFieldName.HOUR);
        Range<Integer> hourRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.HOUR).getConstraints().getValidRange();
        CronField minute = expressions.get(CronFieldName.MINUTE);
        Range<Integer> minuteRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.MINUTE).getConstraints().getValidRange();
        CronField second;
        Range<Integer> secondRange;
        if(expressions.containsKey(CronFieldName.SECOND)){
            second = expressions.get(CronFieldName.SECOND);
            secondRange = cron.getCronDefinition().getFieldDefinition(CronFieldName.SECOND).getConstraints().getValidRange();
        } else {
            second = new CronField(
                    CronFieldName.SECOND,
                    new On(
                            FieldConstraintsBuilder.instance()
                                    .forField(CronFieldName.SECOND).createConstraintsInstance(),
                            new IntegerFieldValue(0))
                    );
            secondRange = Range.all();
        }

        return new StringBuilder()
                .append(descriptor.describe(null, second, secondRange)).append(" ")
                .append(descriptor.describe(second, minute, minuteRange)).append(" ")
                .append(descriptor.describe(minute, hour, hourRange)).append(" ")
                .append(descriptor.describe(hour, dow, dowRange)).append(" ")
                .append(descriptor.describe(dow, dom, domRange)).append(" ")
                .append(descriptor.describe(dom, month, monthRange)).append(" ")
                .append(descriptor.describe(month, year, yearRange))
                .toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Creates an instance with UK locale
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance() {
        return new CronDescriptor();
    }

    /**
     * Creates and instance with given locale
     * @param locale - Locale in which descriptions will be given
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance(Locale locale) {
        return new CronDescriptor(locale);
    }
}
