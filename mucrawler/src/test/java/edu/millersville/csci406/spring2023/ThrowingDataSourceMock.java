package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Set;


/**
 * A DataSource that throws DataSourceExceptions, just so we can see that clients handle them properly.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class ThrowingDataSourceMock extends AbstractCrawlingDataSourceMock {

	/** A Set of CrawlJobs to be processed. */
	private Set<CrawlJob> jobs;
	
	/**
	 * Constructs a new ThrowingDataSourceMock.
	 * 
	 * @param jobs A Set of CrawlJobs to be processed.
	 */
	public ThrowingDataSourceMock(Set<CrawlJob> jobs) {
		this.jobs = jobs;
	}
	
	@Override
	public Set<CrawlJob> getURLsToCrawl() throws DataSourceException {
		return jobs;
	}

	@Override
	public Set<CrawlJob> finishCrawlingHtmlFile(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException {
		throw new DataSourceException(new UnsupportedOperationException());
	}
}
