package com.cronutils.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Simple scheduler for starting jobs using JDKs {@link ScheduledExecutorService}.
 *
 * @param <I> type of ID
 * @author norbertroamsys
 */
public class CronScheduler<I> {

    // the executer service for scheduling the CronJobs.
    private final ScheduledExecutorService executorService;
    // the registry about scheduled jobs
    private final Map<I, CronJob> scheduledCronJobs;

    /**
     * Default constructor using a single thread executor.
     */
    public CronScheduler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    /**
     * Constructor for using a thread executor pool.
     *
     * @param threadPoolSize the number of threads to keep in the pool, even if they are idle
     */
    public CronScheduler(final int threadPoolSize) {
        this(Executors.newScheduledThreadPool(threadPoolSize));
    }

    private CronScheduler(final ScheduledExecutorService executorService) {
        this.executorService = executorService;
        this.scheduledCronJobs = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Adds a job to be executed defined by given cron expression.
     *
     * @param id the unique ID of the job
     * @param cron the cron expression
     * @param job the callback that should be executed
     * @return <code>true</code> if the job has been added successfully
     */
    public boolean addCronJob(final I id, final Cron cron, final Runnable job) {
        if (!scheduledCronJobs.containsKey(id)) {
            scheduledCronJobs.put(id, new CronJob(executorService, ExecutionTime.forCron(cron), job));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the next time the job will be start executing.
     *
     * @param id the unique ID of the job
     * @return the date and time or <code>null</code>
     */
    public ZonedDateTime getNextCronJobExecution(final I id) {
        if (scheduledCronJobs.containsKey(id)) {
            return scheduledCronJobs.get(id).getNextExecution();
        } else {
            return null;
        }
    }

    /**
     * Removes a job formally added. The job execution will also be canceled.
     *
     * @param id the unique ID of the job
     * @return <code>true</code> if the job has been removed successfully
     */
    public boolean removeCronJob(final I id) {
        if (scheduledCronJobs.containsKey(id)) {
            return scheduledCronJobs.remove(id).cancel();
        } else {
            return false;
        }
    }

    /**
     * Shuts down the scheduler by canceling all jobs and also shutting down the {@link ScheduledExecutorService}.
     * <b>Please note that this scheduler can't be used after the shutdown was invoked!</b>
     */
    public void shutdown() {
        scheduledCronJobs.values().forEach(CronJob::cancel);
        scheduledCronJobs.clear();
        executorService.shutdown();
    }
}
