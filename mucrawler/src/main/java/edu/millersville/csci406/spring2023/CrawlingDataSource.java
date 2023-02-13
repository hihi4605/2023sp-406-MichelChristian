package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Set;

/**
 * A source of data for the crawling component of the search engine project.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public interface CrawlingDataSource {
	
	/**
	 * Gets a set of all CrawlJobs that are waiting to be completed.
	 * 
	 * @return A set of all CrawlJobs that are waiting to be completed.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public Set<CrawlJob> getURLsToCrawl() throws DataSourceException;
	
	/**
	 * Marks crawling of a robots.txt file as completed.
	 * Adds rules extracted from that file to the DataSource.
	 * Deletes any CrawlJobs that were waiting to be completed but that are disallowed by the new rules.
	 * 
	 * @param job The CrawlJob for the robots.txt file that was crawled.
	 * @param newRules A set of RobotsRules pulled out of that file.
	 * @return A set of CrawlJobs that were waiting to be completed but that are disallowed by the new rules and were thus deleted.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public Set<CrawlJob> finishCrawlingRobotsFile(CrawlJob job, Set<RobotsRule> newRules) throws DataSourceException;
	
	/**
	 * Marks crawling of an HTML file as completed.
	 * Adds URLs linked to by that file as new CrawlJobs if they do not already exist or violate any rule.
	 * Saves the contents of the HTML file for later processing steps.
	 * 
	 * @param job The CrawlJob for the HTML file that was crawled.
	 * @param newUrls A set of URLs linked to by that file.
	 * @param content The complete HTML contents of that file.
	 * @return A set of CrawlJobs from the newURLs that were not duplicates and that did not violate any rule that would disallow them.  Also includes new robots.txt files if necessary.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public Set<CrawlJob> finishCrawlingHtmlFile(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException;

	/**
	 * Marks crawling of an HTML file as impossible.
	 * 
	 * @param job The CrawlJob for the HTML file that cannot be crawled.
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	public void cancelCrawlingHtmlFile(CrawlJob job) throws DataSourceException;
}

