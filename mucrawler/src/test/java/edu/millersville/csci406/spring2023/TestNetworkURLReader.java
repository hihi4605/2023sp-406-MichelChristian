package edu.millersville.csci406.spring2023;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.nodes.Document;
import org.junit.Test;


/**
 * A collection of unit tests for the NetworkURLReader class.
 * 
 * @author Chad Hogg
 * @version 2023-02-02
 */
public class TestNetworkURLReader {

	/**
	 * Tests reading a robots.txt file from a NetworkURLReader.
	 * 
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 */
	@Test
	public void testReadRobotsTxtFile() throws MalformedURLException {
		URLReader urlReader = new NetworkURLReader();
		try (Scanner input = urlReader.readRobotsTxtFile(new URL("http://www.chadhogg.name/examplerobots.txt"));) {
			String line = input.nextLine();
			assertEquals(line, "User-agent: *");
			line = input.nextLine();
			assertEquals(line, "Disallow:");
			line = input.nextLine();
			assertEquals(line, "");
			line = input.nextLine();
			assertEquals(line, "# too many repeated hits, too quick");
			line = input.nextLine();
			assertEquals(line, "User-agent: litefinder");
			line = input.nextLine();
			assertEquals(line, "Disallow: /");
			line = input.nextLine();
			assertEquals(line, "");
			line = input.nextLine();
			assertEquals(line, "# Yahoo. too many repeated hits, too quick");
			line = input.nextLine();
			assertEquals(line, "User-agent: Slurp");
			line = input.nextLine();
			assertEquals(line, "Disallow: /");
			line = input.nextLine();
			assertEquals(line, "");
			line = input.nextLine();
			assertEquals(line, "# too many repeated hits, too quick");
			line = input.nextLine();
			assertEquals(line, "User-agent: Baidu");
			line = input.nextLine();
			assertEquals(line, "Disallow: /");
			assertFalse(input.hasNextLine());
		}
	}
	
	/**
	 * Tests reading an HTML file from a NetworkURLReader.
	 * Note that this test will fail if the file we open's owner ever decides to edit it.
	 * 
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 */
	@Test
	public void testReadHTMLFile() throws MalformedURLException {
		URLReader urlReader = new NetworkURLReader();
		Document document = urlReader.readHTMLFile(new URL("http://example.com/"));
		assertNotNull(document);
		assertEquals(document.title(), "Example Domain");
		assertEquals(document.body().getElementsByTag("h1").size(), 1);
		assertEquals(document.body().getElementsByTag("h1").get(0).html(), "Example Domain");
	}
	
	/**
	 * Tests trying to read a non-existent HTML file.
	 * Note that this test will fail if someone actually creates the file.
	 * 
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 */
	@Test
	public void testUnknownHost() throws MalformedURLException {
		URLReader urlReader = new NetworkURLReader();
		Document document = urlReader.readHTMLFile(new URL("http://asdladflasdfasdf.com/adsdlfkiuhfapiusudhfaf.html"));
		assertNull(document);
		Scanner input = urlReader.readRobotsTxtFile(new URL("http://asdfdkahsdfiauhsdfia.com/robots.txt"));
		assertNull(input);
	}
	
	/**
	 * Tests trying to read a non-HTML file.
	 * 
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 */
	@Test
	public void testNonHtmlFile() throws MalformedURLException {
		URLReader urlReader = new NetworkURLReader();
		Document document = urlReader.readHTMLFile(new URL("http://www.robotstxt.org/robots.txt"));
		assertNull(document);
	}
}
