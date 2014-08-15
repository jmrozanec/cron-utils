package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.CronField;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Cron {
    private Map<CronFieldName, CronField> fields;

    public Cron(List<CronField> fields){
        this.fields = Maps.newHashMap();
        Validate.notNull(fields);
        for(CronField field : fields){
            this.fields.put(field.getField(), field);
        }
    }

    public CronField retrieve(CronFieldName name){
        return fields.get(name);
    }

    public Map<CronFieldName, CronField> retrieveFieldAsMap(){
        return Collections.unmodifiableMap(fields);
    }
}
