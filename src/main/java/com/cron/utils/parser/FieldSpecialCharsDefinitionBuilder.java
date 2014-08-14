package com.cron.utils.parser;

import com.cron.utils.CronFieldName;

/**
 * Builder that allows to specify properties for a cron field supporting non-standard characters
 */
class FieldSpecialCharsDefinitionBuilder extends FieldDefinitionBuilder {

    /**
     * Constructor
     * @param parserBuilder - ParserDefinitionBuilder
     * @param fieldName - CronFieldName
     */
    public FieldSpecialCharsDefinitionBuilder(ParserDefinitionBuilder parserBuilder, CronFieldName fieldName){
        super(parserBuilder, fieldName);
    }

    /**
     * Registers the field supports the hash (#) special char
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder supportsHash(){
        constraints.addHashSupport();
        return this;
    }

    /**
     * Registers the field supports the L (L) special char
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder supportsL(){
        constraints.addLSupport();
        return this;
    }

    /**
     * Registers the field supports the W (W) special char
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder supportsW(){
        constraints.addWSupport();
        return this;
    }

    /**
     * Defines mapping between integer values with equivalent meaning
     * @param source - higher value
     * @param dest - lower value with equivalent meaning to source
     * @return this FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder withIntMapping(int source, int dest){
        constraints.withIntValueMapping(source, dest);
        return this;
    }
}
