package com.cronutils.scheduler;

import com.cronutils.model.time.ExecutionTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A CronJob scheduled by {@link CronScheduler} that executes a tasks and plans the next execution time.
 *
 * @author norbertroamsys
 */
class CronJob implements Runnable {

    private final ScheduledExecutorService executorService;
    private final ExecutionTime executionTime;
    private final Runnable job;

    private ZonedDateTime nextExecution;
    private ScheduledFuture<?> scheduledFuture;
    private volatile boolean canceled;

    /**
     * Constructor.
     *
     * @param executorService the executer service for sequential scheduling
     * @param executionTime the execution time calculator
     * @param job the callback that should be executed
     */
    CronJob(final ScheduledExecutorService executorService, final ExecutionTime executionTime, final Runnable job) {
        this.executorService = executorService;
        this.executionTime = executionTime;
        this.job = job;
        // do the first planing using current time
        scheduleNext(ZonedDateTime.now());
    }

    /**
     * Returns the next time the job will be start executing.
     *
     * @return the date and time or <code>null</code>
     */
    public ZonedDateTime getNextExecution() {
        return nextExecution;
    }

    @Override
    public void run() {
        if (!canceled) {
            job.run();
        }
        if (!canceled) {
            scheduleNext(nextExecution);
        }
    }

    /**
     * Plans the next execution time to run this job again.
     *
     * @param timeStamp the reference time stamp
     */
    private void scheduleNext(final ZonedDateTime timeStamp) {
        final Optional<ZonedDateTime> next = executionTime.nextExecution(timeStamp);
        if (next.isPresent()) {
            nextExecution = next.get();
            final long delay = ChronoUnit.MILLIS.between(timeStamp, nextExecution);
            scheduledFuture = executorService.schedule(this, delay, TimeUnit.MILLISECONDS);
        } else {
            // job maybe defined to run only once
            scheduledFuture = null;
        }
    }

    /**
     * Cancels the job.
     *
     * @return whether the job has really been canceled
     */
    public boolean cancel() {
        canceled = true;
        if (scheduledFuture != null) {
            return scheduledFuture.cancel(false);
        } else {
            return false;
        }
    }

}