package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A class that controls a web crawl.
 * You should only ever have one instance of this, which will coordinate the
 * work of multiple threads.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class MyCrawlController implements CrawlController {

    /**
     * A map of hostname to queue of CrawlJobs on that host that need to be
     * processed in the future.
     */
    private Map<String, PriorityQueue<CrawlJob>> futureJobs;

    /**
     * A queue of CrawlJobs that should be processed next.
     * Whenever this empties it is filled with one CrawlJob from each entry in
     * futureJobs to spread out concurrent accesses.
     */
    private Queue<CrawlJob> currentJobs;

    /**
     * A map of hostname to last time that a CrawlJob for that host was released.
     */
    private Map<String, Long> lastAccessTimes;

    /** A data source from which we will get and write documents. */
    private CrawlingDataSource dataSource;

    /**
     * The minimum number of milliseconds between releases of CrawlJobs for the same
     * host.
     */
    private long crawlDelay;

    /**
     * Constructs a CrawlController from the DataSource that it will use.
     * This loads all work from the data source into its own data structures.
     * 
     * @param dataSource A DataSource to use throughout the lifetime of the new
     *                   CrawlController.
     * @param crawlDelay The minimum number of milliseconds between releases of
     *                   CrawlJobs for the same host.
     * @throws DataSourceException If there is a problem accessing the DataSource.
     */
    public MyCrawlController(CrawlingDataSource dataSource, long crawlDelay) throws DataSourceException {
        this.dataSource = dataSource;
        this.crawlDelay = crawlDelay;
        futureJobs = new HashMap<>();
        currentJobs = new LinkedList<>();
        lastAccessTimes = new HashMap<>();
        Set<CrawlJob> outstandingJobs = this.dataSource.getURLsToCrawl();
        for (CrawlJob job : outstandingJobs) {
            addJobToFutureQueue(job);
        }
    }

    @Override
    public synchronized CrawlJob getJob() {
        CrawlJob returnValue = null;

        if (currentJobs.isEmpty()) {
            Iterator<Map.Entry<String, PriorityQueue<CrawlJob>>> iter = futureJobs.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, PriorityQueue<CrawlJob>> entry = iter.next();
                currentJobs.add(entry.getValue().remove());
                if (entry.getValue().isEmpty()) {
                    iter.remove();
                }
            }
        }

        if (!currentJobs.isEmpty()) {
            returnValue = currentJobs.remove();
            if (lastAccessTimes.containsKey(returnValue.getURL().getHost())) {
                delay(lastAccessTimes.get(returnValue.getURL().getHost()));
            }
            lastAccessTimes.put(returnValue.getURL().getHost(), System.currentTimeMillis());
        }
        return returnValue;
    }

    @Override
    public synchronized void finishRobots(CrawlJob job, Set<RobotsRule> newRules) throws DataSourceException {
        Set<CrawlJob> toRemove = dataSource.finishCrawlingRobotsFile(job, newRules);
        Iterator<CrawlJob> iter = currentJobs.iterator();
        while (iter.hasNext() && !toRemove.isEmpty()) {
            CrawlJob possibleMatch = iter.next();
            if (toRemove.contains(possibleMatch)) {
                iter.remove();
                toRemove.remove(possibleMatch);
            }
        }
        PriorityQueue<CrawlJob> futureQueue = futureJobs.get(job.getURL().getHost());
        if (futureQueue != null) {
            iter = futureQueue.iterator();
            while (iter.hasNext() && !toRemove.isEmpty()) {
                CrawlJob possibleMatch = iter.next();
                if (toRemove.contains(possibleMatch)) {
                    iter.remove();
                    toRemove.remove(possibleMatch);
                }
            }
            if (futureQueue.isEmpty()) {
                futureJobs.remove(job.getURL().getHost());
            }
        }
    }

    @Override
    public synchronized void finishHtml(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException {
        Set<CrawlJob> toAdd = dataSource.finishCrawlingHtmlFile(job, newUrls, content);
        for (CrawlJob newJob : toAdd) {
            addJobToFutureQueue(newJob);
        }
    }

    @Override
    public synchronized void cancelHtml(CrawlJob job) throws DataSourceException {
        dataSource.cancelCrawlingHtmlFile(job);
    }

    /**
     * Delays until at least crawlDelay milliseconds have passed since some point in
     * the past.
     * 
     * @param previousTime The time from which we should delay (from
     *                     System.currentTimeMillis()).
     */
    private void delay(long previousTime) {
        long currentTime = System.currentTimeMillis();
        long neededDelay = crawlDelay + 5 - (currentTime - previousTime);
        while (neededDelay > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(neededDelay);
            } catch (InterruptedException e) {
                // That's OK, we will just re-enter the loop if necessary.
            }
            currentTime = System.currentTimeMillis();
            neededDelay = crawlDelay + 5 - (currentTime - previousTime);
        }
    }

    /**
     * Adds a job to the complicated queue of future jobs to process.
     * 
     * @param job The new job.
     */
    private void addJobToFutureQueue(CrawlJob job) {
        String host = job.getURL().getHost();
        if (!futureJobs.containsKey(host)) {
            futureJobs.put(host, new PriorityQueue<>());
        }
        futureJobs.get(host).add(job);
    }

}
