package edu.millersville.csci406.spring2023;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * A unit test for the MyCrawlController class.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class TestMyCrawlController {

	/** The initial set of CrawlJobs to be completed. */
	private Set<CrawlJob> initialJobs;
	/**
	 * A Map of CrawlJob (robots.txt) to Set of RobotsRules that should be received
	 * for that CrawlJob.
	 */
	private Map<CrawlJob, Set<RobotsRule>> expectedRules;
	/**
	 * A Map of CrawlJob (robots.txt) to Set of CrawlJobs that should be disallowed
	 * by that CrawlJob.
	 */
	private Map<CrawlJob, Set<CrawlJob>> disallowedJobs;
	/**
	 * A Map of CrawlJob (HTML) to Set of URLs that should be received for that
	 * CrawlJob.
	 */
	private Map<CrawlJob, Set<URL>> expectedUrls;
	/**
	 * A Map of CrawlJob (HTML) to String of content that should be received for
	 * that CrawlJob.
	 */
	private Map<CrawlJob, String> expectedContent;
	/**
	 * A Map of CrawlJob (HTML) to Set of CrawlJobs that should be created for that
	 * CrawlJob.
	 */
	private Map<CrawlJob, Set<CrawlJob>> newJobs;
	/** A Set of CrawlJobs that should be cancelled. */
	private Set<CrawlJob> expectedCancellations;

	/**
	 * Creates collections needed for any test.
	 */
	@Before
	public void setup() {
		initialJobs = new HashSet<>();
		expectedRules = new HashMap<>();
		disallowedJobs = new HashMap<>();
		expectedUrls = new HashMap<>();
		expectedContent = new HashMap<>();
		newJobs = new HashMap<>();
		expectedCancellations = new HashSet<>();
	}

	/**
	 * Tests that the MyCrawlController works correctly when given no jobs to work
	 * on.
	 * 
	 * @throws DataSourceException If there is a problem accessing the DataSource.
	 */
	@Test
	public void testNoJobs() throws DataSourceException {
		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules,
				disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);
		assertNull(controller.getJob());
		dataSource.checkResults();
	}

	/**
	 * Tests that the MyCrawlController works in a realistic scenario with multiple
	 * jobs, including those that create new jobs and that delete existing jobs.
	 * 
	 * @throws DataSourceException   If there is a problem accessing the DataSource.
	 * @throws MalformedURLException If I typed an invalid URL.
	 */
	@Test
	public void testManyJobs() throws DataSourceException, MalformedURLException {
		CrawlJob job1 = new CrawlJob(1, new URL("http://example.kings.edu/robots.txt"));
		CrawlJob job2 = new CrawlJob(2, new URL("http://example.kings.edu/index.html"));
		CrawlJob job3 = new CrawlJob(3, new URL("http://example.wilkes.edu/index.html"));
		CrawlJob job4 = new CrawlJob(4, new URL("http://example.wilkes.edu/robots.txt"));
		CrawlJob job5 = new CrawlJob(5, new URL("http://example.kings.edu/good.html"));
		CrawlJob job6 = new CrawlJob(6, new URL("http://example.kings.edu/bad.html"));
		CrawlJob job7 = new CrawlJob(7, new URL("http://example.misericordia.edu/index.html"));
		CrawlJob job8 = new CrawlJob(8, new URL("http://example.misericordia.edu/robots.txt"));
		CrawlJob job9 = new CrawlJob(9, new URL("http://example.scranton.edu"));

		initialJobs.add(job1);
		initialJobs.add(job2);
		initialJobs.add(job3);
		initialJobs.add(job4);
		initialJobs.add(job9);

		Set<RobotsRule> rules1 = new HashSet<>();
		rules1.add(new RobotsRule("http", "example.kings.edu", "/", true));
		rules1.add(new RobotsRule("http", "example.kings.edu", "/bad.html", false));
		expectedRules.put(job1, rules1);
		Set<RobotsRule> rules4 = new HashSet<>();
		rules4.add(new RobotsRule("http", "example.wilkes.edu", "/", false));
		expectedRules.put(job4, rules4);
		Set<RobotsRule> rules8 = new HashSet<>();
		rules8.add(new RobotsRule("http", "example.misericordia.edu", "/", true));
		expectedRules.put(job8, rules8);

		Set<CrawlJob> disallowed1 = new HashSet<>();
		disallowed1.add(job6);
		disallowedJobs.put(job1, disallowed1);
		Set<CrawlJob> disallowed4 = new HashSet<>();
		disallowed4.add(job3);
		disallowedJobs.put(job4, disallowed4);
		Set<CrawlJob> disallowed8 = new HashSet<>();
		disallowedJobs.put(job8, disallowed8);

		Set<URL> urls2 = new HashSet<>();
		urls2.add(job5.getURL());
		urls2.add(job6.getURL());
		urls2.add(job7.getURL());
		expectedUrls.put(job2, urls2);
		Set<URL> urls5 = new HashSet<>();
		urls5.add(job7.getURL());
		expectedUrls.put(job5, urls5);
		Set<URL> urls7 = new HashSet<>();
		expectedUrls.put(job7, urls7);

		expectedContent.put(job2, "ABC");
		expectedContent.put(job5, "DEF");
		expectedContent.put(job7, "GHI");

		Set<CrawlJob> newJobs2 = new HashSet<>();
		newJobs2.add(job5);
		newJobs2.add(job7);
		newJobs2.add(job8);
		newJobs.put(job2, newJobs2);
		Set<CrawlJob> newJobs5 = new HashSet<>();
		newJobs.put(job5, newJobs5);
		Set<CrawlJob> newJobs7 = new HashSet<>();
		newJobs.put(job7, newJobs7);

		expectedCancellations.add(job9);

		List<CrawlJob> finishedJobs = new ArrayList<>();

		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules,
				disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 0);

		CrawlJob currentJob = controller.getJob();
		while (currentJob != null) {
			if (currentJob.getURL().getFile().equals("/robots.txt")) {
				controller.finishRobots(currentJob, expectedRules.get(currentJob));
			} else {
				if (expectedCancellations.contains(currentJob)) {
					controller.cancelHtml(currentJob);
				} else {
					controller.finishHtml(currentJob, expectedUrls.get(currentJob), expectedContent.get(currentJob));
					boolean found = false;
					int i = 0;
					while (i < finishedJobs.size() && found == false) {
						CrawlJob finishedJob = finishedJobs.get(i);
						if (finishedJob.getURL().getHost().equals(currentJob.getURL().getHost())
								&& finishedJob.getURL().getFile().equals("/robots.txt")) {
							found = true;
						}
						i++;
					}
					assertTrue(found);
				}
			}
			if (!finishedJobs.isEmpty()) {
				assertTrue(!finishedJobs.get(finishedJobs.size() - 1).getURL().getHost()
						.equals(currentJob.getURL().getHost()));
			}
			finishedJobs.add(currentJob);
			currentJob = controller.getJob();
		}

		dataSource.checkResults();
	}

	/**
	 * Tests that having a robots.txt file delete jobs that are already in the
	 * current queue works correctly.
	 * 
	 * @throws DataSourceException   If there is a problem accessing the DataSource.
	 * @throws MalformedURLException If I typed an invalid URL.
	 */
	@Test
	public void testDeleteJobsInCurrentQueue() throws DataSourceException, MalformedURLException {
		CrawlJob job1 = new CrawlJob(1, new URL("http://example.kings.edu/robots.txt"));
		CrawlJob job2 = new CrawlJob(2, new URL("http://example.wilkes.edu/robots.txt"));
		CrawlJob job3 = new CrawlJob(3, new URL("http://example.misericordia.edu/robots.txt"));
		CrawlJob job4 = new CrawlJob(4, new URL("http://example.kings.edu/index.html"));
		CrawlJob job5 = new CrawlJob(5, new URL("http://example.wilkes.edu/index.html"));
		CrawlJob job6 = new CrawlJob(6, new URL("http://example.misericordia.edu/index.html"));
		CrawlJob job7 = new CrawlJob(7, new URL("http://example.kings.edu/foo.html"));

		initialJobs.add(job1);
		initialJobs.add(job2);
		initialJobs.add(job3);
		initialJobs.add(job4);
		initialJobs.add(job5);
		initialJobs.add(job6);
		initialJobs.add(job7);

		expectedRules.put(job1, new HashSet<>());
		expectedRules.put(job2, new HashSet<>());
		expectedRules.put(job3, new HashSet<>());

		Set<CrawlJob> disallowed2 = new HashSet<>();
		disallowed2.add(job5);
		disallowedJobs.put(job2, disallowed2);
		disallowedJobs.put(job1, new HashSet<>());
		disallowedJobs.put(job3, new HashSet<>());

		expectedUrls.put(job4, new HashSet<>());
		expectedUrls.put(job6, new HashSet<>());
		expectedUrls.put(job7, new HashSet<>());

		expectedContent.put(job4, "ABC");
		expectedContent.put(job6, "JKL");
		expectedContent.put(job7, "MNO");

		newJobs.put(job4, new HashSet<>());
		newJobs.put(job6, new HashSet<>());
		newJobs.put(job7, new HashSet<>());

		List<CrawlJob> finishedJobs = new ArrayList<>();

		CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs, expectedRules,
				disallowedJobs, expectedUrls, expectedContent, newJobs, expectedCancellations);
		MyCrawlController controller = new MyCrawlController(dataSource, 2);

		CrawlJob currentJob = controller.getJob();
		CrawlJob nextJob = controller.getJob();
		while (currentJob != null) {
			if (currentJob.getURL().getFile().equals("/robots.txt")) {
				controller.finishRobots(currentJob, expectedRules.get(currentJob));
			} else {
				controller.finishHtml(currentJob, expectedUrls.get(currentJob), expectedContent.get(currentJob));
			}
			finishedJobs.add(currentJob);
			currentJob = nextJob;
			nextJob = controller.getJob();
		}

		dataSource.checkResults();
		assertTrue(finishedJobs.contains(job1));
		assertTrue(finishedJobs.contains(job2));
		assertTrue(finishedJobs.contains(job3));
		assertTrue(finishedJobs.contains(job4));
		assertFalse(finishedJobs.contains(job5));
		assertTrue(finishedJobs.contains(job6));
		assertTrue(finishedJobs.contains(job7));
	}

	/**
	 * Tests that even when an InterruptedException is thrown, CrawlJobs from the
	 * same host are still spread out as much as desired.
	 * 
	 * @throws InterruptedException If the tester itself somehow gets interrupted.
	 */
	@Test
	public void testInterruption() throws InterruptedException {
		final int DELAY = 1000;

		/**
		 * A class that allows me to put most of the testing code into another thread
		 * that I can interrupt.
		 */
		class InterruptibleRunner implements Runnable {

			/**
			 * A Map of host name to List of times at which CrawlJobs for that host were
			 * released.
			 */
			private Map<String, List<Long>> finishTimes;

			/**
			 * Constructs an InterruptibleRunner.
			 */
			public InterruptibleRunner() {
				finishTimes = new HashMap<>();
			}

			@Override
			public void run() {
				try {
					initialJobs.add(new CrawlJob(1, new URL("http://example.kings.edu/foo.html")));
					initialJobs.add(new CrawlJob(2, new URL("http://example.kings.edu/bar.html")));
					initialJobs.add(new CrawlJob(3, new URL("http://example.kings.edu/baz.html")));
					initialJobs.add(new CrawlJob(4, new URL("http://example.wilkes.edu/foo.html")));
					initialJobs.add(new CrawlJob(5, new URL("http://example.wilkes.edu/bar.html")));
					initialJobs.add(new CrawlJob(6, new URL("http://example.wilkes.edu/baz.html")));
					initialJobs.add(new CrawlJob(7, new URL("http://example.misericordia.edu/foo.html")));
					initialJobs.add(new CrawlJob(8, new URL("http://example.misericordia.edu/bar.html")));
					initialJobs.add(new CrawlJob(9, new URL("http://example.misericordia.edu/baz.html")));
					CrawlControllerDataSourceMock dataSource = new CrawlControllerDataSourceMock(initialJobs,
							expectedRules, disallowedJobs, expectedUrls, expectedContent, newJobs,
							expectedCancellations);
					MyCrawlController controller = new MyCrawlController(dataSource, DELAY);

					CrawlJob currentJob = controller.getJob();
					while (currentJob != null) {
						if (!finishTimes.containsKey(currentJob.getURL().getHost())) {
							finishTimes.put(currentJob.getURL().getHost(), new ArrayList<>());
						}
						finishTimes.get(currentJob.getURL().getHost()).add(System.currentTimeMillis());
						currentJob = controller.getJob();
					}
				} catch (MalformedURLException exception) {
					fail("This really can't happen.");
				} catch (DataSourceException exception) {
					fail("This shouldn't be possible either.");
				}
			}

			/**
			 * Gets the finish times.
			 * 
			 * @return A Map of hostname to List of times at which a CrawlJob was released.
			 */
			public Map<String, List<Long>> getFinishTimes() {
				return finishTimes;
			}
		}

		InterruptibleRunner runner = new InterruptibleRunner();
		Thread otherThread = new Thread(runner);
		otherThread.start();

		otherThread.interrupt();
		Thread.sleep(DELAY + 1);
		otherThread.interrupt();
		Thread.sleep(DELAY / 4);
		otherThread.interrupt();
		Thread.sleep(DELAY / 4);
		otherThread.interrupt();
		otherThread.join();

		Map<String, List<Long>> finishTimes = runner.getFinishTimes();
		assertTrue(finishTimes.size() == 3);
		assertTrue(finishTimes.containsKey("example.kings.edu"));
		assertTrue(finishTimes.containsKey("example.wilkes.edu"));
		assertTrue(finishTimes.containsKey("example.misericordia.edu"));
		long earliest = finishTimes.get("example.kings.edu").get(0);
		long latest = earliest;
		for (List<Long> value : finishTimes.values()) {
			assertTrue(value.size() == 3);
			assertTrue(value.get(1) >= value.get(0) + DELAY);
			assertTrue(value.get(2) >= value.get(1) + DELAY);
			for (Long time : value) {
				if (time < earliest) {
					earliest = time;
				} else if (time > latest) {
					latest = time;
				}
			}
		}
		assertTrue("It looks like you are delaying more often than you should be.", latest <= earliest + DELAY * 3);
	}

}
