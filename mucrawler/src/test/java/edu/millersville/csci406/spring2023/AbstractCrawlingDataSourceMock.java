package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Set;


/**
 * A superclass of all CrawlingDataSources used for testing.
 * This is a dummy class that makes it so that when testing a specific feature you will not need to implement unneeded DataSource functionality.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class AbstractCrawlingDataSourceMock implements CrawlingDataSource {

	@Override
	public Set<CrawlJob> getURLsToCrawl() throws DataSourceException {
		throw new UnsupportedOperationException("You must override this method if you intend to use it.");
	}

	@Override
	public Set<CrawlJob> finishCrawlingRobotsFile(CrawlJob job, Set<RobotsRule> newRules) throws DataSourceException {
		throw new UnsupportedOperationException("You must override this method if you intend to use it.");
	}

	@Override
	public Set<CrawlJob> finishCrawlingHtmlFile(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException {
		throw new UnsupportedOperationException("You must override this method if you intend to use it.");
	}

	@Override
	public void cancelCrawlingHtmlFile(CrawlJob job) throws DataSourceException {
		throw new UnsupportedOperationException("You must override this method if you intend to use it.");		
	}

}
