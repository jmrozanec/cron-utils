package com.cronutils.zrefactor.main;

import com.cronutils.model.CronType;
import com.cronutils.zrefactor.model.definition.CronDefinitionBuilder;
import com.cronutils.zrefactor.parser.CronParser;

public class Main {
    public static void main(String[] args) {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        System.out.println(parser.parse("0 30 10-13 ? * WED-FRI").asString());
    }
}
