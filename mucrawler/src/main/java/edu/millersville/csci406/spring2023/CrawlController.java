package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Set;

/**
 * A class that controls a web crawl.
 * You should only ever have one instance of this, which will coordinate the work of multiple threads.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public interface CrawlController {
	
	/** A default delay of 10 seconds between accesses to the same host. */
	public static final int DEFAULT_CRAWL_DELAY = 10000;
		
	/**
	 * Gets a CrawlJob to work on, or null if there are none.
	 * This will delay if necessary to avoid releasing CrawlJobs for the same host too close together.
	 * It will never release an HTML file for a protocol/host pair before releasing a robots.txt file for it.
	 * It will interleave jobs for different hosts as much as possible.
	 * 
	 * @return A CrawlJob to work on, or null if there are none.
	 */
	public CrawlJob getJob();
	
	/**
	 * Records a set of rules from a robots.txt file, marks that file as completed, and deletes all future jobs that the rules would disallow.
	 * 
	 * @param job The CrawlJob for the robots.txt file that was parsed.
	 * @param newRules A set of RobotsRules generated from that file.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public void finishRobots(CrawlJob job, Set<RobotsRule> newRules) throws DataSourceException;
	
	/**
	 * Records the content of an HTML file, marks that file as completed, and considers all URLs linked to by that document as new jobs.
	 * Note that some URLs may not become new jobs, either because they already exist as jobs or completed documents, or because some rule disallows them from being crawled.
	 * 
	 * @param job The CrawlJob for the HTML document that was parsed.
	 * @param newUrls A set of URLs from the links contained within the document.
	 * @param content The complete HTML content of the document.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public void finishHtml(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException;
	
	/**
	 * Cancels the processing of an HTML file and marks it as impossible.
	 * 
	 * @param job The CrawlJob for the HTML file that will not be parsed.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public void cancelHtml(CrawlJob job) throws DataSourceException;
	
}
