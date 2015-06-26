package org.jboss.aerogear.unifiedpush.message.cache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ServiceDisposalScheduler {

    private ScheduledExecutorService scheduler;
    private long terminationTimeout = 10000L;

    @PostConstruct
    public void initialize() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @PreDestroy
    public void terminate() {
        try {
            scheduler.shutdown();
            scheduler.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted during attempt to shutdown gracefully", e);
        }
    }

    public void scheduleForDisposal(DisposableReference<?> reference, long delay) {
        synchronized (scheduler) {
            terminationTimeout = Math.max(delay + 2500L, terminationTimeout);
            scheduler.schedule(new DisposeTask(reference), delay, TimeUnit.MILLISECONDS);
        }
    }


    private static class DisposeTask implements Runnable {

        private DisposableReference<?> reference;

        public DisposeTask(DisposableReference<?> reference) {
            this.reference = reference;
        }

        @Override
        public void run() {
            reference.dispose();
        }
    }
}
