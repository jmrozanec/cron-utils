package com.cron.utils.mapper;

import com.cron.utils.CronFieldName;
import com.cron.utils.model.Cron;
import com.cron.utils.model.CronDefinition;
import com.cron.utils.model.FieldDefinition;
import com.cron.utils.parser.field.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CronMapper {
    private Map<CronFieldName, Function<CronField, CronField>> mappings;

    public CronMapper(CronDefinition from, CronDefinition to){
        mappings = Maps.newHashMap();
        buildMappings(from, to);
    }

    public Cron map(Cron cron) {
        List<CronField> fields = Lists.newArrayList();
        for(CronFieldName name : CronFieldName.values()){
            if(mappings.containsKey(name)){
                fields.add(mappings.get(name).apply(cron.retrieve(name)));
            }
        }
        return new Cron(fields);
    }

    private void buildMappings(CronDefinition from, CronDefinition to){
        Map<CronFieldName, FieldDefinition> sourceFieldDefinitions = Maps.newHashMap();
        Map<CronFieldName, FieldDefinition> destFieldDefinitions = Maps.newHashMap();
        for(FieldDefinition fieldDefinition : from.getFieldDefinitions()){
            sourceFieldDefinitions.put(fieldDefinition.getFieldName(), fieldDefinition);
        }
        for(FieldDefinition fieldDefinition : to.getFieldDefinitions()){
            destFieldDefinitions.put(fieldDefinition.getFieldName(), fieldDefinition);
        }
        boolean startedDestMapping = false;
        boolean startedSourceMapping = false;
        for(CronFieldName name : CronFieldName.values()){
            if(destFieldDefinitions.get(name)!=null){
                startedDestMapping = true;
            }
            if(sourceFieldDefinitions.get(name)!=null){
                startedSourceMapping = true;
            }
            if(startedDestMapping && destFieldDefinitions.get(name) == null){
                break;
            }
            //destination has fields before source definition starts. We default them to zero.
            if(!startedSourceMapping && sourceFieldDefinitions.get(name) == null && destFieldDefinitions.get(name) != null){
                mappings.put(name, returnOnZeroExpression(name));
            }
            //destination has fields after source definition was processed. We default them to always.
            if(startedSourceMapping && sourceFieldDefinitions.get(name) == null && destFieldDefinitions.get(name) != null){
                mappings.put(name, returnAlwaysExpression(name));
            }
            if(sourceFieldDefinitions.get(name) != null && destFieldDefinitions.get(name) != null){
                mappings.put(name, returnSameExpression());
            }
        }
    }

    private Function<CronField, CronField> returnSameExpression(){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return field;
            }
        };
    }

    private Function<CronField, CronField> returnOnZeroExpression(final CronFieldName name){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return new CronField(name, new On(FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance(),"0"));
            }
        };
    }

    private Function<CronField, CronField> returnAlwaysExpression(final CronFieldName name){
        return new Function<CronField, CronField>() {
            @Override
            public CronField apply(CronField field) {
                return new CronField(name, new Always(FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance()));
            }
        };
    }
}
