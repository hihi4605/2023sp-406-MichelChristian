package edu.millersville.csci406.spring2023;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A collection of unit tests for CrawlWorkerThread.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class TestCrawlWorkerThread {

	/** The initial set of CrawlJobs to be completed. */
	private Set<CrawlJob> initialJobs;
	/** A Map of CrawlJob (robots.txt) to Set of RobotsRules that should be received for that CrawlJob. */
	private Map<CrawlJob, Set<RobotsRule>> expectedRules;
	/** A Map of CrawlJob (robots.txt) to Set of CrawlJobs that should be disallowed by that CrawlJob. */
	private Map<CrawlJob, Set<CrawlJob>> disallowedJobs;
	/** A Map of CrawlJob (HTML) to Set of URLs that should be received for that CrawlJob. */
	private Map<CrawlJob, Set<URL>> expectedUrls;
	/** A Map of CrawlJob (HTML) to String of content that should be received for that CrawlJob. */
	private Map<CrawlJob, String> expectedContent;
	/** A Map of CrawlJob (HTML) to Set of CrawlJobs that should be created for that CrawlJob. */
	private Map<CrawlJob, Set<CrawlJob>> newJobs;
	/** A Set of CrawlJobs that should be cancelled. */
	private Set<CrawlJob> expectedCancellations;
	/** A Map of URLs to their contents. */
	private Map<URL, String> fileContents;
	/** A LogCapturer. */
	private LogCapturer logCapturer;
	
	/**
	 * Constructs a new TestCrawlWorkerThread.
	 */
	public TestCrawlWorkerThread() {
		logCapturer = new LogCapturer();
	}
	
	/**
	 * Creates collections needed for any test and starts capturing log output.
	 */
	@Before
	public void setup() {
		logCapturer.startCapture();
		initialJobs = new HashSet<>();
		expectedRules = new HashMap<>();
		disallowedJobs = new HashMap<>();
		expectedUrls = new HashMap<>();
		expectedContent = new HashMap<>();
		newJobs = new HashMap<>();
		expectedCancellations = new HashSet<>();
		fileContents = new HashMap<>();
	}
	
	/**
	 * Ends capturing log output.
	 */
	@After
	public void tearDown() {
		logCapturer.endCapture();
	}
	
	/**
	 * Tests that when there are no jobs to be done, the thread just exits gracefully.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 */
	@Test
	public void test00NoJobs() throws DataSourceException, InterruptedException {
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that when we are looking for a non-existent robots.txt file, we create a rule that everything is allowed.
	 * This is because some administrators will not write a robots.txt file.
	 * We won't know this until we try (and fail) to open it.
	 * The lack of a file should mean that we have no restrictions.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test01NoRobotsTxtFile() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://nosuchfile.com/robots.txt"));
		initialJobs.add(onlyJob);
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "nosuchfile.com", "/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that when the robots.txt file only contains rules for other crawlers, we create a rule that everything is allowed.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test02NoRulesForUs() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://noneforus.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("User-Agent: googlebot\n");
		builder.append("Disallow: /scripts/\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "noneforus.com", "/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that when the robots.txt file only contains an empty disallow, we create a rule that everything is allowed.
	 * This is just a weird special case of how the file is supposed to be interpreted.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test03EmptyDisallow() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://nothingdisallowed.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("User-Agent: *\n");
		builder.append("Disallow:\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "nothingdisallowed.com", "/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
		
	/**
	 * Tests that when the robots.txt file contains several rules for us, all are found.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test04SeveralRules() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://severalrules.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("User-Agent: *\n");
		builder.append("Disallow: /scripts/\n");
		builder.append("Disallow: /private/\n");
		builder.append("Allow: /scripts/examples/\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/private/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/examples/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that comments and other miscellaneous lines in robots.txt files are ignored.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test05CommentsEtc() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://severalrules.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("# This is a comment.\n");
		builder.append("User-Agent: *\n");
		builder.append("Sitemap: something or other\n");
		builder.append("Disallow: /scripts/\n");
		builder.append("Crawl-delay: 8000 years\n");
		builder.append("Host: parasite\n");
		builder.append("Disallow: /private/\n");
		builder.append("Allow: /scripts/examples/\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/private/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/examples/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that comments on the same line as a directive in robots.txt files are ignored.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test06CommentsOnLine() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://severalrules.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("# This is a comment.\n");
		builder.append("User-Agent: *\n");
		builder.append("Sitemap: something or other\n");
		builder.append("Disallow: /scripts/ # Comment after a space.\n");
		builder.append("Crawl-delay: 8000 years\n");
		builder.append("Host: parasite\n");
		builder.append("Disallow: /private/#Comment without a space.\n");
		builder.append("Allow: /scripts/examples/\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/private/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "severalrules.com", "/scripts/examples/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that directives in other records are ignored.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test07MultipleRecords() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://multipledirectives.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("User-Agent: googlebot\n");
		builder.append("Disallow: /nogoogle/\n");
		builder.append("\n");
		builder.append("User-Agent: *\n");
		builder.append("Disallow: /scripts/\n");
		builder.append("Disallow: /private/\n");
		builder.append("Allow: /scripts/examples/\n");
		builder.append("\n");
		builder.append("User-Agent: microsoft\n");
		builder.append("Disallow: /nobing/\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "multipledirectives.com", "/scripts/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "multipledirectives.com", "/private/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "multipledirectives.com", "/scripts/examples/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}

	/**
	 * Tests that nonsense lines in robots.txt files are ignored and do not cause crashes.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test08MalformedRobots() throws DataSourceException, InterruptedException, MalformedURLException {
		StringBuilder builder = new StringBuilder();
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://badfile.com/robots.txt"));
		initialJobs.add(onlyJob);
		builder.delete(0, builder.length());
		builder.append("What is this?\n");
		builder.append("User-Agent: *\n");
		builder.append("Disallow: /scripts/\n");
		builder.append("Disallow: /private/\n");
		builder.append("Dis\n");
		builder.append("Allow: /scripts/examples/\n");
		builder.append("Just ignore these lines!\n");
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedRules.put(onlyJob, new HashSet<>());
		expectedRules.get(onlyJob).add(new RobotsRule("http", "badfile.com", "/scripts/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "badfile.com", "/private/", false));
		expectedRules.get(onlyJob).add(new RobotsRule("http", "badfile.com", "/scripts/examples/", true));
		disallowedJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that when we are looking for a non-existent HTML file, we cancel the crawl.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test09NoHtmlFile() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://nosuchfile.com/"));
		initialJobs.add(onlyJob);
		expectedCancellations.add(onlyJob);
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}

	/**
	 * Tests that when an HTML file has no links, we do not find any.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test10NoLinks() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://nolinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}

	/**
	 * Tests that when an HTML file has the most straightforward kind of links we find them.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test11SimpleLinks() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://haslinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("    <a href=\"http://www.example.com/\">One</a>\n");
		builder.append("    <a href=\"http://www.example.com/foo\">Two</a>\n");
		builder.append("    <a href=\"http://www.example.com/bar.html\">Three</a>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		expectedUrls.get(onlyJob).add(new URL("http://www.example.com/"));
		expectedUrls.get(onlyJob).add(new URL("http://www.example.com/foo"));
		expectedUrls.get(onlyJob).add(new URL("http://www.example.com/bar.html"));
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that relative links get converted into absolute links.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test12RelativeLinks() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://haslinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("    <a href=\"store.html\">Four</a>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		expectedUrls.get(onlyJob).add(new URL("http://haslinks.com/store.html"));
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that non-http/https links get ignored.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test13WrongSchemeLinks() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://haslinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("    <a href=\"http://haslinks.com/foo.html\">Five</a>\n");
		builder.append("    <a href=\"ftp://haslinks.com/bar.html\">Six</a>\n");
		builder.append("    <a href=\"https://haslinks.com/baz.html\">Seven</a>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		expectedUrls.get(onlyJob).add(new URL("http://haslinks.com/foo.html"));
		expectedUrls.get(onlyJob).add(new URL("https://haslinks.com/baz.html"));
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	/**
	 * Tests that links with no path get a slash.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test14LinkWithNoPath() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://haslinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("    <a href=\"http://www.other.com\">Eight</a>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		expectedUrls.get(onlyJob).add(new URL("http://www.other.com/"));
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();
	}
	
	/**
	 * Tests that links with non-standard ports get ignored.
	 * 
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 * @throws MalformedURLException  If there is an error in the test itself.
	 */
	@Test
	public void test15LinksWithPorts() throws DataSourceException, InterruptedException, MalformedURLException {
		CrawlJob onlyJob = new CrawlJob(1, new URL("http://haslinks.com/"));
		initialJobs.add(onlyJob);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("  <head>\n");
		builder.append("    <title>Some Title</title>\n");
		builder.append("  </head>\n");
		builder.append("  <body>\n");
		builder.append("    <p>A paragraph!</p>\n");
		builder.append("    <p>Another paragraph!</p>\n");
		builder.append("    <a href=\"http://haslinks.com/foo.html\">Nine</a>\n");
		builder.append("    <a href=\"http://haslinks.com:8080/bar.html\">Ten</a>\n");
		builder.append("    <a href=\"https://haslinks.com/baz.html\">Eleven</a>\n");
		builder.append("  </body>");
		builder.append("</html>\n");
		expectedContent.put(onlyJob, builder.toString());
		fileContents.put(onlyJob.getURL(), builder.toString());
		expectedUrls.put(onlyJob, new HashSet<>());
		expectedUrls.get(onlyJob).add(new URL("http://haslinks.com/foo.html"));
		expectedUrls.get(onlyJob).add(new URL("https://haslinks.com/baz.html"));
		newJobs.put(onlyJob, new HashSet<>());
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();		
	}
	
	

	/**
	 * Tests that the CrawlWorkerThread works correctly in a short realistic scenario.
	 * 
	 * @throws MalformedURLException Only if there is a problem in the test itself.
	 * @throws DataSourceException Should be impossible.
	 * @throws InterruptedException If the thread gets interrupted.
	 */
	@Test
	public void test16ManyJobs() throws MalformedURLException, DataSourceException, InterruptedException {
		StringBuilder builder = new StringBuilder();
		
		CrawlJob job1 = new CrawlJob(1, new URL("http://example.kings.edu/"));
		CrawlJob job2 = new CrawlJob(2, new URL("http://example.kings.edu/robots.txt"));
		CrawlJob job3 = new CrawlJob(3, new URL("http://asdfasdfasdfasdf.com/"));
		CrawlJob job4 = new CrawlJob(4, new URL("http://asdfasdfasdfasdf.com/robots.txt"));
		CrawlJob job5 = new CrawlJob(5, new URL("http://example.wilkes.edu/robots.txt"));
		CrawlJob job6 = new CrawlJob(6, new URL("http://example.misericordia.edu/robots.txt"));
		
		initialJobs.add(job1);
		initialJobs.add(job2);
		initialJobs.add(job3);
		initialJobs.add(job4);
		initialJobs.add(job5);
		initialJobs.add(job6);
		
		builder.append("<html>\n");
		builder.append(" <head>\n");
		builder.append("  <title>Hello World!</title>\n");
		builder.append(" </head>\n");
		builder.append(" <body>\n");
		builder.append("  <p>I am a paragraph.  ");
		builder.append("I contain a <a href=\"http://example.kings.edu/foo.html\">link</a> and ");
		builder.append("<a href=\"https://example.kings.edu\">another</a> and ");
		builder.append("<a href=\"http://example.kings.edu:8000/baz.html\">one more</a>.  ");
		builder.append("and a <a href=\"ftp://example.com/\">ftp one</a>.)</p>\n");
		builder.append(" </body>\n");
		builder.append("</html>");
		fileContents.put(job1.getURL(), builder.toString());

		builder.delete(0, builder.length());
		builder.append("# This line is a comment.\n");
		builder.append("User-agent: Google\n");
		builder.append("Disallow: /bar/\n");
		builder.append("\n");
		builder.append("User-agent: *\n");
		builder.append("Disallow: /scripts/ # Comment after it.\n");
		builder.append("Disallow:\n");
		builder.append("Allow: /examples/\n");
		builder.append("Disallow: /personal\n");
		builder.append("Allow: /free#Dumb\n");
		builder.append("Crawl-delay: 5\n");
		builder.append("# Comment\n");
		builder.append("Sitemap: http://example.com/sitemap.xml\n");
		builder.append("asdfasdf\n");
		builder.append("\n");
		builder.append("User-agent: Yahoo\n");
		builder.append("Disallow: /foo/\n");
		builder.append("\n");
		fileContents.put(job2.getURL(), builder.toString());
		
		builder.delete(0, builder.length());
		builder.append("User-agent: Google\n");
		builder.append("Disallow:\n");
		builder.append("\n");
		fileContents.put(job5.getURL(), builder.toString());
		
		builder.delete(0, builder.length());
		builder.append("User-agent: *\n");
		builder.append("Allow:");
		fileContents.put(job6.getURL(), builder.toString());
		
		Set<RobotsRule> rules2 = new HashSet<>();
		rules2.add(new RobotsRule("http", "example.kings.edu", "/scripts/", false));
		rules2.add(new RobotsRule("http", "example.kings.edu", "/", true));
		rules2.add(new RobotsRule("http", "example.kings.edu", "/examples/", true));
		rules2.add(new RobotsRule("http", "example.kings.edu", "/personal", false));
		rules2.add(new RobotsRule("http", "example.kings.edu", "/free", true));
		expectedRules.put(job2, rules2);
		Set<RobotsRule> rules5 = new HashSet<>();
		rules5.add(new RobotsRule("http", "example.wilkes.edu", "/", true));
		expectedRules.put(job5, rules5);
		Set<RobotsRule> rules4 = new HashSet<>();
		rules4.add(new RobotsRule("http", "asdfasdfasdfasdf.com", "/", true));
		expectedRules.put(job4, rules4);
		Set<RobotsRule> rules6 = new HashSet<>();
		rules6.add(new RobotsRule("http", "example.misericordia.edu", "/", true));
		expectedRules.put(job6, rules6);
		
		disallowedJobs.put(job2, new HashSet<>());
		disallowedJobs.put(job5, new HashSet<>());
		disallowedJobs.put(job4, new HashSet<>());
		disallowedJobs.put(job6, new HashSet<>());
		
		Set<URL> urls1 = new HashSet<>();
		urls1.add(new URL("http://example.kings.edu/foo.html"));
		urls1.add(new URL("https://example.kings.edu/"));
		expectedUrls.put(job1, urls1);
		
		expectedContent.put(job1, fileContents.get(job1.getURL()));
		
		newJobs.put(job1, new HashSet<>());
		
		expectedCancellations.add(job3);
		
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();
	}
	
	/**
	 * Exercises the exception handling in {@link CrawlWorkerThread#run()}.
	 * 
	 * @throws DataSourceException If there is a problem communicating with the DataSource.
	 * @throws MalformedURLException If there is a problem in the test itself.
	 */
	@Test
	public void test17Throwing() throws DataSourceException, MalformedURLException {
		CrawlJob job = new CrawlJob(1, new URL("http://example.com/"));
		Set<CrawlJob> jobs = new HashSet<>();
		jobs.add(job);
		ThrowingDataSourceMock dataSource = new ThrowingDataSourceMock(jobs);
		Map<URL, String> fileContents = new HashMap<>();
		fileContents.put(job.getURL(), "<html> <head> <title>A</title> </head> <body> <p>B</p> </body> </html>");
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		worker.run();
		String loggedOutput = logCapturer.getMessages();
		assertTrue(loggedOutput.contains("Thread exiting due to exception."));
	}
	
	/**
	 * Tests that we correctly read multiple applicable records in a robots.txt file, even when there are other records in between them.
	 * 
	 * @throws DataSourceException If there is a problem communicating with the DataSource.
	 * @throws MalformedURLException If there is a problem in the test itself.
	 * @throws InterruptedException If the thread gets interrupted.
	 */
	@Test
	public void test18MultipleRecords() throws DataSourceException, MalformedURLException, InterruptedException {
		CrawlJob job = new CrawlJob(1, new URL("http://example.com/robots.txt"));
		initialJobs.add(job);
		StringBuilder builder = new StringBuilder();
		builder.append("User-agent: *\n");
		builder.append("Disallow: /foo/\n");
		builder.append("Disallow: /bar/\n");
		builder.append("\n");
		builder.append("User-agent: Google\n");
		builder.append("Disallow: /nogoogle/\n");
		builder.append("\n");
		builder.append("User-agent: *\n");
		builder.append("Disallow: /baz/\n");
		builder.append("\n");
		builder.append("User-agent: Yahoo\n");
		builder.append("Disallow: /noyahoo/\n");
		builder.append("\n");
		fileContents.put(job.getURL(), builder.toString());

		Set<RobotsRule> rules = new HashSet<>();
		rules.add(new RobotsRule("http", "example.com", "/foo/", false));
		rules.add(new RobotsRule("http", "example.com", "/bar/", false));
		rules.add(new RobotsRule("http", "example.com", "/baz/", false));
		expectedRules.put(job, rules);
		
		disallowedJobs.put(job, new HashSet<>());

		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		URLReaderMock urlReader = new URLReaderMock(fileContents);
		CrawlWorkerThread worker = new CrawlWorkerThread(controller, urlReader);
		Thread thread = new Thread(worker);
		thread.start();
		thread.join();
		dataSource.checkResults();
	}
}
