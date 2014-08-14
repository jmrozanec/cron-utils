package com.cron.utils.parser;

import com.cron.utils.CronFieldName;

class FieldSpecialCharsDefinitionBuilder extends FieldDefinitionBuilder {
    
    public FieldSpecialCharsDefinitionBuilder(ParserDefinitionBuilder parserBuilder, CronFieldName fieldName){
        super(parserBuilder, fieldName);
    }

    public FieldSpecialCharsDefinitionBuilder supportsHash(){
        constraints.addHashSupport();
        return this;
    }

    public FieldSpecialCharsDefinitionBuilder supportsL(){
        constraints.addLSupport();
        return this;
    }

    public FieldSpecialCharsDefinitionBuilder supportsW(){
        constraints.addWSupport();
        return this;
    }

    public FieldSpecialCharsDefinitionBuilder withIntMapping(int source, int dest){
        constraints.withIntValueMapping(source, dest);
        return this;
    }
}
