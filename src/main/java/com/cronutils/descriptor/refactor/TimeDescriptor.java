package com.cronutils.descriptor.refactor;

import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;

public class TimeDescriptor {

    private final ResourceBundle resourceBundle;

    public TimeDescriptor(final ResourceBundle resourceBundle) {
        this.resourceBundle = Preconditions.checkNotNull(resourceBundle, "The resource bundle must not be null");
    }


    public String describe(final Cron cron) {
        return describe(cron.retrieveFieldsAsMap());
    }

    private String describe(final Map<CronFieldName, CronField> expressions) {

        if (expressions.containsKey(CronFieldName.SECOND)) {
            final CronField cronField = expressions.get(CronFieldName.SECOND);
            if (cronField.getExpression() instanceof Always) {
                return describeEverySecond(1);
            } else if (cronField.getExpression() instanceof On) {
                return describeAtSecond(((On)cronField.getExpression()).getTime().getValue());
            } else if (cronField.getExpression() instanceof Every) {
                return describeEverySecond(((Every)cronField.getExpression()).getPeriod().getValue());
            }
        }


        return StringUtils.EMPTY;
    }

    private String describeEverySecond(final int second) {
        final double[] secondsLimit = {1, 2};
        final String[] secondsStrings = {
                resourceBundle.getString("oneSecond"),
                resourceBundle.getString("multipleSeconds")
        };
        final double[] everyLimit = {1,2};
        final String[] everyStrings = {
                resourceBundle.getString("every_one"),
                resourceBundle.getString("every_multi")
        };

        final ChoiceFormat secondsChoiceFormat = new ChoiceFormat(secondsLimit, secondsStrings);
        final ChoiceFormat everyChoiceFormat = new ChoiceFormat(everyLimit, everyStrings);
        final String pattern = resourceBundle.getString("pattern_every_seconds");

        final MessageFormat messageFormat = new MessageFormat(pattern, Locale.UK);

        final Format[] formats = { everyChoiceFormat, secondsChoiceFormat, NumberFormat.getInstance() };
        messageFormat.setFormats(formats);
        final Object[] messageArguments = {second, second, second };
        final String result = messageFormat.format(messageArguments);
        return result;
    }

    private String describeAtSecond(final int second) {

        final String pattern = resourceBundle.getString("pattern_at_second");

        final MessageFormat messageFormat = new MessageFormat(pattern, Locale.UK);
        final Format[] formats = {NumberFormat.getInstance()};
        messageFormat.setFormats(formats);

        final Object[] messageArguments = { second };
        final String result = messageFormat.format(messageArguments);
        return result;
    }

}
