package edu.millersville.csci406.spring2023;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A collection of unit tests for the RobotsRule class.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class TestRobotsRule {

	/**
	 * Tests all features of the RobotsRule class.
	 */
	@Test
	public void testAll() {
		RobotsRule rule1 = new RobotsRule("http", "example.kings.edu", "/", true);
		assertEquals(rule1.getProtocol(), "http");
		assertEquals(rule1.getHostName(), "example.kings.edu");
		assertEquals(rule1.getPathPrefix(), "/");
		assertTrue(rule1.isAllowed());
		assertEquals(rule1.toString(), "http://example.kings.edu/ true");
		RobotsRule rule2 = new RobotsRule("https", "example.wilkes.edu", "/scripts/", false);
		assertEquals(rule2.getProtocol(), "https");
		assertEquals(rule2.getHostName(), "example.wilkes.edu");
		assertEquals(rule2.getPathPrefix(), "/scripts/");
		assertFalse(rule2.isAllowed());
		assertEquals(rule2.toString(), "https://example.wilkes.edu/scripts/ false");
		assertTrue(rule1.equals(rule1));
		assertFalse(rule1.equals(rule2));
		assertEquals(rule1.hashCode(), "http".hashCode() + ("example.kings.edu".hashCode() * 3) + ("/".hashCode() * 7));
		assertFalse(rule1.equals(null));
		assertFalse(rule1.equals(this));
	}
}
