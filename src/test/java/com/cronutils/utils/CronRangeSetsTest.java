package com.cronutils.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.parser.CronParser;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import static org.junit.Assert.*;

public class CronRangeSetsTest {

    @Test
    public void getRangesetsForValidExpressionUnix() throws Exception {
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser quartz = new CronParser(quartzcd);
        final Cron cron = quartz.parse("0 * * * *");
        Map<CronFieldName, RangeSet<Integer>> rangesets = CronUtils.asRangeSets(cron).getRangesets();
        Map<CronFieldName, RangeSet<Integer>> expected = new HashMap<>();

        RangeSet<Integer> minutes = TreeRangeSet.create();
        minutes.add(Range.singleton(0));
        RangeSet<Integer> hours = TreeRangeSet.create();
        hours.add(Range.closed(0, 23));
        RangeSet<Integer> dom = TreeRangeSet.create();
        dom.add(Range.closed(1, 31));
        RangeSet<Integer> dow = TreeRangeSet.create();
        dow.add(Range.closed(0, 7));
        RangeSet<Integer> month = TreeRangeSet.create();
        month.add(Range.closed(1, 12));

        expected.put(CronFieldName.MINUTE, minutes);
        expected.put(CronFieldName.HOUR, hours);
        expected.put(CronFieldName.DAY_OF_MONTH, dom);
        expected.put(CronFieldName.DAY_OF_WEEK, dow);
        expected.put(CronFieldName.MONTH, month);

        for(CronFieldName fieldName : CronUtils.asRangeSets(cron).getRangesets().keySet()){
            RangeSet<Integer> expectedrs = expected.get(fieldName);
            RangeSet<Integer> computed = rangesets.get(fieldName);
            assertEquals(expectedrs, computed);
        }
    }

    @Test
    public void getRangesetsForValidExpressionQuartz() throws Exception {
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser quartz = new CronParser(quartzcd);
        final Cron cron = quartz.parse("0 * * ? * MON *");
        Map<CronFieldName, RangeSet<Integer>> rangesets = CronUtils.asRangeSets(cron).getRangesets();
        Map<CronFieldName, RangeSet<Integer>> expected = new HashMap<>();

        RangeSet<Integer> seconds = TreeRangeSet.create();
        seconds.add(Range.singleton(0));
        RangeSet<Integer> minutes = TreeRangeSet.create();
        minutes.add(Range.closed(0, 59));
        RangeSet<Integer> hours = TreeRangeSet.create();
        hours.add(Range.closed(0, 23));
        RangeSet<Integer> dom = TreeRangeSet.create();
        RangeSet<Integer> dow = TreeRangeSet.create();
        dow.add(Range.singleton(2));
        RangeSet<Integer> month = TreeRangeSet.create();
        month.add(Range.closed(1, 12));
        RangeSet<Integer> year = TreeRangeSet.create();
        year.add(Range.closed(1970, 2099));

        expected.put(CronFieldName.SECOND, seconds);
        expected.put(CronFieldName.MINUTE, minutes);
        expected.put(CronFieldName.HOUR, hours);
        expected.put(CronFieldName.DAY_OF_MONTH, dom);
        expected.put(CronFieldName.DAY_OF_WEEK, dow);
        expected.put(CronFieldName.MONTH, month);
        expected.put(CronFieldName.YEAR, year);

        for(CronFieldName fieldName : CronUtils.asRangeSets(cron).getRangesets().keySet()){
            RangeSet<Integer> expectedrs = expected.get(fieldName);
            RangeSet<Integer> computed = rangesets.get(fieldName);
            assertEquals(expectedrs, computed);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRangesetsForInvalidExpression() throws Exception {
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser quartz = new CronParser(quartzcd);
        final Cron cron = quartz.parse("0 * * L * ? *");
        CronUtils.asRangeSets(cron);
    }

}