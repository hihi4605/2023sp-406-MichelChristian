package edu.millersville.csci406.spring2023;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;


/**
 * A collection of unit tests for the CrawlJob class.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class TestCrawlJob {

	/**
	 * Tests the CrawlJob class under all normal conditions.
	 * 
	 * @throws MalformedURLException Only if I wrote the test incorrectly.
	 */
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testNormalConditions() throws MalformedURLException {
		CrawlJob[] jobs = new CrawlJob[5];
		jobs[0] = new CrawlJob(1, new URL("http://example.kings.edu/index.html"));
		jobs[1] = new CrawlJob(2, new URL("http://example.kings.edu/robots.txt"));
		jobs[2] = new CrawlJob(3, new URL("https://example.kings.edu/robots.txt"));
		jobs[3] = new CrawlJob(4, new URL("https://example.kings.edu/index.html"));
		jobs[4] = new CrawlJob(5, new URL("http://example.kings.edu/foo.html"));
		
		assertEquals(jobs[0].getId(), 1);
		assertEquals(jobs[0].getURL().getProtocol(), "http");
		assertEquals(jobs[0].getURL().getHost(), "example.kings.edu");
		assertEquals(jobs[0].getURL().getFile(), "/index.html");
		assertEquals(jobs[0].toString(), "1 http://example.kings.edu/index.html");
		
		for(int i = 0; i < jobs.length; i++) {
			for(int j = 0; j < jobs.length; j++) {
				if(i == j) {
					assertTrue(jobs[i].equals(jobs[j]));
				}
				else {
					assertFalse(jobs[i].equals(jobs[j]));
				}
			}
			assertEquals(jobs[i].hashCode(), i + 1);
		}
		
		assertFalse(jobs[0].equals(null));
		assertFalse(jobs[0].equals(this));
		
		assertEquals(jobs[0].compareTo(jobs[0]), 0);
		assertEquals(jobs[0].compareTo(jobs[1]), 1);
		assertEquals(jobs[0].compareTo(jobs[2]), 1);
		assertEquals(jobs[0].compareTo(jobs[3]), -1);
		assertEquals(jobs[1].compareTo(jobs[0]), -1);
		assertEquals(jobs[1].compareTo(jobs[1]), 0);
		assertEquals(jobs[1].compareTo(jobs[2]), -1);
		assertEquals(jobs[1].compareTo(jobs[3]), -1);
		assertEquals(jobs[2].compareTo(jobs[0]), -1);
		assertEquals(jobs[2].compareTo(jobs[1]), 1);
		assertEquals(jobs[2].compareTo(jobs[2]), 0);
		assertEquals(jobs[2].compareTo(jobs[3]), -1);
		assertEquals(jobs[3].compareTo(jobs[0]), 1);
		assertEquals(jobs[3].compareTo(jobs[1]), 1);
		assertEquals(jobs[3].compareTo(jobs[2]), 1);
		assertEquals(jobs[3].compareTo(jobs[3]), 0);
		assertEquals(jobs[0].compareTo(jobs[4]), -1);
		assertEquals(jobs[4].compareTo(jobs[0]), 1);
	}
	
	/**
	 * Tests that {@link CrawlJob#compareTo(CrawlJob)} throws a NullPointerException when it should.
	 * 
	 * @throws MalformedURLException Only if I wrote the test incorrectly.
	 */
	@Test(expected = NullPointerException.class)
	public void testCompareToNull() throws MalformedURLException {
		CrawlJob job = new CrawlJob(1, new URL("http://example.kings.edu/"));
		job.compareTo(null);
	}
}
