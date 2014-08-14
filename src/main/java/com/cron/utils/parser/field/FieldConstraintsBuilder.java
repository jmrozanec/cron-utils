package com.cron.utils.parser.field;

import com.cron.utils.CronFieldName;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class FieldConstraintsBuilder {
    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private int startRange;
    private int endRange;
    private Set<SpecialChar> specialChars;

    private FieldConstraintsBuilder() {
        stringMapping = Maps.newHashMap();
        intMapping = Maps.newHashMap();
        startRange = 0;//no negatives!
        endRange = Integer.MAX_VALUE;
        specialChars = Sets.newHashSet();
        specialChars.add(SpecialChar.NONE);
    }

    public FieldConstraintsBuilder forField(CronFieldName field) {
        switch (field) {
            case SECOND:
            case MINUTE:
                endRange = 59;
                return this;
            case HOUR:
                endRange = 23;
                return this;
            case DAY_OF_WEEK:
                stringMapping = daysOfWeekMapping();
                endRange = 6;
                return this;
            case DAY_OF_MONTH:
                startRange = 1;
                endRange = 31;
                return this;
            case MONTH:
                stringMapping = monthsMapping();
                startRange = 1;
                endRange = 12;
                return this;
            default:
                return this;
        }
    }

    public FieldConstraintsBuilder addHashSupport() {
        specialChars.add(SpecialChar.HASH);
        return this;
    }

    public FieldConstraintsBuilder addLSupport() {
        specialChars.add(SpecialChar.L);
        return this;
    }

    public FieldConstraintsBuilder addWSupport() {
        specialChars.add(SpecialChar.W);
        return this;
    }

    public FieldConstraintsBuilder withIntValueMapping(int source, int dest){
        intMapping.put(source, dest);
        return this;
    }

    public FieldConstraints createConstraintsInstance(){
        return new FieldConstraints(stringMapping, intMapping, specialChars, startRange, endRange);
    }

    private static Map<String, Integer> daysOfWeekMapping() {
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("MON", 1);
        stringMapping.put("TUE", 2);
        stringMapping.put("WED", 3);
        stringMapping.put("THU", 4);
        stringMapping.put("FRI", 5);
        stringMapping.put("SAT", 6);
        stringMapping.put("SUN", 7);
        return stringMapping;
    }

    private static Map<String, Integer> monthsMapping() {
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("JAN", 1);
        stringMapping.put("FEB", 2);
        stringMapping.put("MAR", 3);
        stringMapping.put("APR", 4);
        stringMapping.put("MAY", 5);
        stringMapping.put("JUN", 6);
        stringMapping.put("JUL", 7);
        stringMapping.put("AUG", 8);
        stringMapping.put("SEP", 9);
        stringMapping.put("OCT", 10);
        stringMapping.put("NOV", 11);
        stringMapping.put("DEC", 12);
        return stringMapping;
    }

    public static FieldConstraintsBuilder instance(){
        return new FieldConstraintsBuilder();
    }
}
