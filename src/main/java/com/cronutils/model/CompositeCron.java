package com.cronutils.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.utils.Preconditions;

public class CompositeCron implements Cron {
    private Pattern split = Pattern.compile("\\|");
    private List<Cron> crons;
    private CronDefinition definition;

    public CompositeCron(List<Cron> crons){
        this.crons = Collections.unmodifiableList(crons);
        Preconditions.checkNotNullNorEmpty(crons, "List of Cron cannot be null or empty");
        this.definition = crons.get(0).getCronDefinition();
        Preconditions.checkArgument(crons.size()==crons.stream().filter(c->c.getCronDefinition().equals(definition)).count(), "All Cron objects must have same definition for CompositeCron");
    }

    public List<Cron> getCrons() {
        return crons;
    }

    @Override
    public CronField retrieve(CronFieldName name) {
        throw new UnsupportedOperationException("Currently not supported for CompositeCron");
    }

    @Override
    public Map<CronFieldName, CronField> retrieveFieldsAsMap() {
        throw new UnsupportedOperationException("Currently not supported for CompositeCron");
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        List<String> patterns = crons.stream().map(Cron::asString).collect(Collectors.toList());
        int fields = patterns.get(0).split(" ").length;
        for(int j=0;j<fields;j++){
            StringBuilder fieldbuilder = new StringBuilder();
            for(String pattern : patterns){
                fieldbuilder.append(String.format("%s ", pattern.split(" ")[j]));
            }
            String fieldstring = fieldbuilder.toString().trim().replaceAll(" ", "|");
            if(split.splitAsStream(fieldstring).distinct().limit(2).count() <= 1){
                fieldstring = split.split(fieldstring)[0];
            }
            builder.append(String.format("%s ", fieldstring));
        }
        return builder.toString().trim();
    }

    @Override
    public CronDefinition getCronDefinition() {
        return definition;
    }

    @Override
    public Cron validate() {
        for(Cron cron : crons){
            cron.validate();
        }
        return this;
    }

    @Override
    public boolean equivalent(CronMapper cronMapper, Cron cron) {
        throw new UnsupportedOperationException("Currently not supported for CompositeCron");
    }

    @Override
    public boolean equivalent(Cron cron) {
        return asString().equals(cron.asString());
    }
}
