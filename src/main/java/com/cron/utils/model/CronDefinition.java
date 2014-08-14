package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CronDefinition {
    private Map<CronFieldName, FieldDefinition> fieldDefinitions;
    private boolean lastFieldOptional;

    public CronDefinition(List<FieldDefinition> fieldDefinitions, boolean lastFieldOptional){
        this.fieldDefinitions = Maps.newHashMap();
        Validate.notNull(fieldDefinitions);
        for(FieldDefinition field : fieldDefinitions){
            this.fieldDefinitions.put(field.getFieldName(), field);
        }
        this.lastFieldOptional = lastFieldOptional;
    }

    public boolean isLastFieldOptional() {
        return lastFieldOptional;
    }

    public Set<FieldDefinition> getFieldDefinitions(){
        return new HashSet<FieldDefinition>(fieldDefinitions.values());
    }
}
