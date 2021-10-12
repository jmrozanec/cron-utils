package com.cronutils.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link CronScheduler}.
 *
 * @author norbertroamsys
 */
public class CronSchedulerTest {

    private int secondsJobRunCount;
    private int twoSecondsJobRunCount;

    /**
     * Tests scheduler by processing two parallel jobs for overall 15 seconds.
     */
    @Test
    public void testScheduler() throws InterruptedException {
        final CronScheduler<Integer> cronScheduler = new CronScheduler<>();
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

        final Cron everySecondJob = parser.parse("*/1 * * * * *");
        final Cron everyTwoSecondsJob = parser.parse("*/2 * * * * *");

        // register two jobs
        Assert.assertTrue(cronScheduler.addCronJob(1, everySecondJob, () -> secondsJobRunCount++));
        Assert.assertTrue(cronScheduler.addCronJob(2, everyTwoSecondsJob, () -> twoSecondsJobRunCount++));

        // let the jobs do there work for 10 seconds
        Thread.sleep(10000);

        Assert.assertTrue("Every-second job should be called", secondsJobRunCount > 0);
        Assert.assertTrue("Every-two-seconds job should be called", twoSecondsJobRunCount > 0);
        Assert.assertTrue("Every-second job should be called not more than 10 times", secondsJobRunCount <= 10);
        Assert.assertTrue("Every-two-second job should be called not more than 5 times", twoSecondsJobRunCount <= 5);
        Assert.assertTrue("Every-second job should be called more often than every-two-seconds job", secondsJobRunCount > twoSecondsJobRunCount);

        // take a snapshot for the counts so far
        final int secondsJobRunCountSnapshot = secondsJobRunCount;
        final int twoSecondsJobRunCountSnapshot = twoSecondsJobRunCount;

        Assert.assertTrue(cronScheduler.removeCronJob(1));

        // let one job do more work for 5 seconds
        Thread.sleep(5000);

        Assert.assertEquals("Every-second job should no longer be called because it has been removed", secondsJobRunCountSnapshot, secondsJobRunCount);
        Assert.assertTrue("Every-two-second job should be called again because it is still active", secondsJobRunCount > twoSecondsJobRunCountSnapshot);

        // shutdown completely
        cronScheduler.shutdown();

        Assert.assertFalse("Every-two-second job should already be removed by shutdown", cronScheduler.removeCronJob(2));
    }

}
