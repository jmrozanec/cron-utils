package com.cronutils.model.field.definition;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import org.apache.commons.lang3.Validate;

public class FieldDayOfWeekDefinitionBuilder extends FieldSpecialCharsDefinitionBuilder {
    private int mondayDoWValue = 1;//default is cron-utils specification
    /**
     * Constructor
     *
     * @param cronDefinitionBuilder - ParserDefinitionBuilder instance -
     *                              if null, a NullPointerException will be raised
     * @param fieldName             - CronFieldName instance -
     */
    public FieldDayOfWeekDefinitionBuilder(CronDefinitionBuilder cronDefinitionBuilder, CronFieldName fieldName) {
        super(cronDefinitionBuilder, fieldName);
        Validate.isTrue(CronFieldName.DAY_OF_WEEK.equals(fieldName), "CronFieldName must be DAY_OF_WEEK");
    }

    /**
     * Registers the field supports the W (W) special char
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withMondayDoWValue(int mondayDoW){
        this.mondayDoWValue = mondayDoW;
        return this;
    }

    /**
     * Registers CronField in ParserDefinitionBuilder and returns its instance
     * @return ParserDefinitionBuilder instance obtained from constructor
     */
    public CronDefinitionBuilder and(){
        cronDefinitionBuilder.register(new DayOfWeekFieldDefinition(fieldName, constraints.createConstraintsInstance(), mondayDoWValue));
        return cronDefinitionBuilder;
    }

    /**
     * Allows to set a range of valid values for field.
     * @param startRange - start range value
     * @param endRange - end range value
     * @return same FieldDayOfWeekDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withValidRange(int startRange, int endRange){
        super.withValidRange(startRange, endRange);
        return this;
    }

    /**
     * Defines mapping between integer values with equivalent meaning
     * @param source - higher value
     * @param dest - lower value with equivalent meaning to source
     * @return this FieldDayOfWeekDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withIntMapping(int source, int dest){
        super.withIntMapping(source, dest);
        return this;
    }
}
