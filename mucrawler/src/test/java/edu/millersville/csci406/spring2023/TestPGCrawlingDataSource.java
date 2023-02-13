package edu.millersville.csci406.spring2023;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A collection of unit tests for the PGCrawlingDataSource class.
 * 
 * @author Chad Hogg
 * @version 2023-02-02
 */
public class TestPGCrawlingDataSource {

	/** A connection to the same database used by the PGCrawlingDataSource. */
	private Connection connection;
	
	/** The PGCrawlingDataSource being tested. */
	private PGCrawlingDataSource dataSource;
	
	/**
	 * Constructs a new TestDatabaseDataSource.
	 * All of the interesting work happens in {@link TestPGCrawlingDataSource#setup()} to allow resource de-allocation.
	 */
	public TestPGCrawlingDataSource() {
		dataSource = null;
		connection = null;
	}
	
	/**
	 * Sets up for a test by restoring the database tables to an empty state.
	 * 
	 * @throws SQLException If there is a problem executing SQL statements.
	 */
	@Before
	public void setup() throws SQLException {
		dataSource = new PGCrawlingDataSource(ConfigurationDefaults.DEFAULT_HOST_NAME, ConfigurationDefaults.DEFAULT_DATABASE_NAME, ConfigurationDefaults.DEFAULT_USER_NAME, ConfigurationDefaults.DEFAULT_PASSWORD, "test");
		connection = DriverManager.getConnection("jdbc:postgresql://" + ConfigurationDefaults.DEFAULT_HOST_NAME + "/" + ConfigurationDefaults.DEFAULT_DATABASE_NAME, ConfigurationDefaults.DEFAULT_USER_NAME, ConfigurationDefaults.DEFAULT_PASSWORD);
		connection.setSchema("test");
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("DELETE FROM document");
			stmt.executeUpdate("DELETE FROM url");
			stmt.executeUpdate("DELETE FROM robots_txt_rule");
			stmt.executeUpdate("DELETE FROM host;");
			stmt.executeUpdate("DELETE FROM extension_blacklist");
			stmt.executeUpdate("DELETE FROM host_blacklist");
			stmt.executeUpdate("DELETE FROM host_whitelist");
			stmt.executeUpdate("ALTER SEQUENCE host_host_id_seq RESTART");
			stmt.executeUpdate("ALTER SEQUENCE url_url_id_seq RESTART");
		}
	}
	
	/**
	 * Tears down after a test by releasing resources used by the PGCrawlingDataSource.
	 * 
	 * @throws SQLException If there is a problem closing the Connection.
	 */
	@After
	public void tearDown() throws SQLException {
		dataSource.close();
		connection.close();
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#getURLsToCrawl()} returns an empty set when there are no URLs in the database.
	 * 
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 */
	@Test
	public void testGetUrlsToCrawlEmpty() throws DataSourceException {
		Set<CrawlJob> jobs = dataSource.getURLsToCrawl();
		assertTrue(jobs.isEmpty());
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#getURLsToCrawl()} returns an empty set when all URLs in the database are also documents.
	 * 
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 * @throws SQLException If there is a problem interacting with the database.
	 */
	@Test
	public void testGetUrlsToCrawlAllDocuments() throws DataSourceException, SQLException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/', NOW())");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/', NOW())");
			stmt.executeUpdate("INSERT INTO document VALUES (1, NULL, 'asdf')");
			stmt.executeUpdate("INSERT INTO document VALUES (2, NULL, 'qwer')");
			Set<CrawlJob> jobs = dataSource.getURLsToCrawl();
			assertTrue(jobs.isEmpty());
		}
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#getURLsToCrawl()} returns an appropriate set when there are URLs that are not documents.
	 * 
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 * @throws SQLException  If there is a problem interacting with the database.
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 */
	@Test
	public void testGetUrlsToCrawlNormal() throws SQLException, DataSourceException, MalformedURLException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/', NOW())");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/academics/')");
			stmt.executeUpdate("INSERT INTO document VALUES (1, NULL, 'asdf')");
			Set<CrawlJob> jobs = dataSource.getURLsToCrawl();
			assertTrue(jobs.size() == 2);
			assertTrue(jobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
			assertTrue(jobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/academics/"))));
		}
	}

	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingRobotsFile(CrawlJob, Set)} throws an exception when the CrawlJob does not match a URL in the database.
	 * 
	 * @throws DataSourceException If the test succeeded.
	 * @throws MalformedURLException If there is an error in the test itself.
	 */
	@Test(expected = DataSourceException.class)
	public void testFinishRobotsBadId() throws MalformedURLException, DataSourceException {
		dataSource.finishCrawlingRobotsFile(new CrawlJob(1, new URL("http", "example.kings.edu", "/robots.txt")), new HashSet<>());
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingRobotsFile(CrawlJob, Set)} works correctly in the common case.
	 * 
	 * @throws SQLException If there is a problem interacting with the database.
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 * @throws DataSourceException If there is a problem interacting with the DataSource.
	 */
	@Test
	public void testFinishRobotsNormal() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/academics/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/robots.txt')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/foo/')");
			stmt.executeUpdate("INSERT INTO document VALUES (1, NULL, 'asdf')");
			CrawlJob robotsJob = new CrawlJob(4, new URL("http", "example.kings.edu", "/robots.txt"));
			Set<RobotsRule> rules = new HashSet<>();
			rules.add(new RobotsRule("http", "example.kings.edu", "/", true));
			rules.add(new RobotsRule("http", "example.kings.edu", "/a", false));
			Set<CrawlJob> deletedJobs = dataSource.finishCrawlingRobotsFile(robotsJob, rules);
			assertTrue(deletedJobs.size() == 2);
			assertTrue(deletedJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
			assertTrue(deletedJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/academics/"))));
			Set<RobotsRule> rulesInDatabase = new HashSet<>();
			try(ResultSet resultsRules = stmt.executeQuery("SELECT protocol, host_id, path_prefix, directive FROM robots_txt_rule");) {
				while(resultsRules.next()) {
					rulesInDatabase.add(new RobotsRule(resultsRules.getString(1), "example.kings.edu", resultsRules.getString(3), resultsRules.getBoolean(4)));
				}
			}
			assertEquals(rules, rulesInDatabase);
			Set<CrawlJob> jobsInDatabase = new HashSet<>();
			try(ResultSet resultsUrls = stmt.executeQuery("SELECT url_id, protocol, host_id, path FROM url WHERE when_crawled IS NULL");) {
				while(resultsUrls.next()) {
					jobsInDatabase.add(new CrawlJob(resultsUrls.getInt(1), new URL(resultsUrls.getString(2), "example.kings.edu", resultsUrls.getString(4))));
				}
			}
			assertTrue(jobsInDatabase.size() == 2);
			assertTrue(jobsInDatabase.contains(new CrawlJob(1, new URL("http", "example.kings.edu", "/"))));
			assertTrue(jobsInDatabase.contains(new CrawlJob(5, new URL("http", "example.kings.edu", "/foo/"))));
		}
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} throws an exception for a bad CrawlJob.
	 * 
	 * @throws DataSourceException If the test succeeded.
	 * @throws MalformedURLException If there is an error in the test itself.
	 */
	@Test(expected = DataSourceException.class)
	public void testFinishCrawlingHtmlFileException() throws MalformedURLException, DataSourceException {
		dataSource.finishCrawlingHtmlFile(new CrawlJob(1, new URL("http", "example.kings.edu", "/")), new HashSet<URL>(), "asdf");
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} works in a typical case.
	 * 
	 * @throws SQLException If there is a problem interacting with the database.
	 * @throws MalformedURLException If there is an error in the test itself.
	 * @throws DataSourceException If there is a problem interacting with the DataSource.
	 */
	@Test
	public void testFinishCrawlingHtmlFileNormal() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/academics/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/robots.txt')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/foo/')");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('kings.edu')");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('wilkes.edu')");
			
			CrawlJob oldJob = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			newUrls.add(new URL("http", "example.kings.edu", "/admissions/"));
			newUrls.add(new URL("http", "example.kings.edu", "/bar/"));
			newUrls.add(new URL("http", "example.wilkes.edu", "/"));
			
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(oldJob, newUrls, "This is some content.");
			
			assertEquals(newJobs.size(), 3);
			// This is a brittle test, because the numbers could be in a different order without being wrong.
			assertTrue(newJobs.contains(new CrawlJob(8, new URL("http", "example.kings.edu", "/bar/"))));
			assertTrue(newJobs.contains(new CrawlJob(6, new URL("http", "example.wilkes.edu", "/"))));
			assertTrue(newJobs.contains(new CrawlJob(7, new URL("http", "example.wilkes.edu", "/robots.txt"))));
			
			try(ResultSet resultsContent = stmt.executeQuery("SELECT content FROM document WHERE url_id = 1");) {
				assertTrue(resultsContent.next());
				assertEquals(resultsContent.getString(1), "This is some content.");
			}
			
			Set<CrawlJob> allJobs = new HashSet<>();
			try(ResultSet resultsAllJobs = stmt.executeQuery("SELECT url_id, protocol, host_name, path FROM url NATURAL JOIN host WHERE url_id NOT IN (SELECT url_id FROM document)");) {
				while(resultsAllJobs.next()) {
					allJobs.add(new CrawlJob(resultsAllJobs.getInt(1), new URL(resultsAllJobs.getString(2), resultsAllJobs.getString(3), resultsAllJobs.getString(4))));
				}
			}
			assertEquals(allJobs.size(), 7);
			assertTrue(allJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
			assertTrue(allJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/academics/"))));
			assertTrue(allJobs.contains(new CrawlJob(4, new URL("http", "example.kings.edu", "/robots.txt"))));
			assertTrue(allJobs.contains(new CrawlJob(5, new URL("http", "example.kings.edu", "/foo/"))));
			assertTrue(allJobs.contains(new CrawlJob(8, new URL("http", "example.kings.edu", "/bar/"))));
			assertTrue(allJobs.contains(new CrawlJob(6, new URL("http", "example.wilkes.edu", "/"))));
			assertTrue(allJobs.contains(new CrawlJob(7, new URL("http", "example.wilkes.edu", "/robots.txt"))));			
		}
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} correctly enforces the whitelists and blacklists.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException If there is an error in the test itself.
	 * @throws DataSourceException If there is a problem communicating with the PGCrawlingDataSource.
	 */
	@Test
	public void testFinishCrawlingHtmlFileBlacklists() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('kings.edu')");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('wilkes.edu')");
			stmt.executeUpdate("INSERT INTO host_blacklist VALUES ('private.kings.edu')");
			stmt.executeUpdate("INSERT INTO extension_blacklist VALUES ('.jpg')");
			stmt.executeUpdate("INSERT INTO extension_blacklist VALUES ('.css')");
			
			CrawlJob oldJob = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			// Should actually be added.
			newUrls.add(new URL("http", "example.kings.edu", "/admissions/"));
			// Should be removed because it is not on the host whitelist.
			newUrls.add(new URL("http", "example.misericordia.edu", "/bar/"));
			// Should be removed because it is an exact match on the host blacklist.
			newUrls.add(new URL("http", "private.kings.edu", "/"));
			// Should be removed because it ends with something on the extension blacklist.
			newUrls.add(new URL("http", "example.kings.edu", "/logo.jpg"));
			// Should actually be added.
			newUrls.add(new URL("https", "kings.edu", "/foo.html"));
			
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(oldJob, newUrls, "This is some content.");
			
			assertEquals(newJobs.size(), 4);
			assertTrue(newJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
			assertTrue(newJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/robots.txt"))));
			assertTrue(newJobs.contains(new CrawlJob(4, new URL("https", "kings.edu", "/foo.html"))));
			assertTrue(newJobs.contains(new CrawlJob(5, new URL("https", "kings.edu", "/robots.txt"))));
			
			Set<CrawlJob> allJobs = new HashSet<>();
			try(ResultSet resultsAllJobs = stmt.executeQuery("SELECT url_id, protocol, host_name, path FROM url NATURAL JOIN host WHERE url_id NOT IN (SELECT url_id FROM document)");) {
				while(resultsAllJobs.next()) {
					allJobs.add(new CrawlJob(resultsAllJobs.getInt(1), new URL(resultsAllJobs.getString(2), resultsAllJobs.getString(3), resultsAllJobs.getString(4))));
				}
			}
			assertEquals(allJobs.size(), 4);
			assertTrue(allJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
			assertTrue(allJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/robots.txt"))));
			assertTrue(allJobs.contains(new CrawlJob(4, new URL("https", "kings.edu", "/foo.html"))));
			assertTrue(allJobs.contains(new CrawlJob(5, new URL("https", "kings.edu", "/robots.txt"))));
		}
	}

	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} handles robots.txt file rules correctly.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException Only if there is an error in the test itself.
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 */
	@Test
	public void testFinishCrawlingHtmlFileRobots() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.wilkes.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO url VALUES (10000, 'http', 1, '/robots.txt', NOW())");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('kings.edu')");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('http', 1, '/', true)");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('http', 1, '/scripts/', false)");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('http', 1, '/scripts/public/', true)");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('http', 2, '/scripts/public/f', false)");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('https', 1, '/f', false)");
			
			CrawlJob oldJob = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			// Should actually be added.
			newUrls.add(new URL("http", "example.kings.edu", "/foo.html"));
			// Should be skipped.
			newUrls.add(new URL("http", "example.kings.edu", "/scripts/"));
			// Should be skipped.
			newUrls.add(new URL("http", "example.kings.edu", "/scripts/foo.html"));
			// Should actually be added.
			newUrls.add(new URL("http", "example.kings.edu", "/scripts/public/"));
			// Should actually be added.
			newUrls.add(new URL("http", "example.kings.edu", "/scripts/public/foo.html"));
			
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(oldJob, newUrls, "This is some content.");
			
			assertEquals(newJobs.size(), 3);
			assertTrue(newJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/foo.html"))));
			assertTrue(newJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/scripts/public/"))));
			assertTrue(newJobs.contains(new CrawlJob(4, new URL("http", "example.kings.edu", "/scripts/public/foo.html"))));
			
			Set<CrawlJob> allJobs = new HashSet<>();
			try(ResultSet resultsAllJobs = stmt.executeQuery("SELECT url_id, protocol, host_name, path FROM url NATURAL JOIN host WHERE when_crawled IS NULL");) {
				while(resultsAllJobs.next()) {
					allJobs.add(new CrawlJob(resultsAllJobs.getInt(1), new URL(resultsAllJobs.getString(2), resultsAllJobs.getString(3), resultsAllJobs.getString(4))));
				}
			}
			assertEquals(allJobs.size(), 3);
			assertTrue(allJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/foo.html"))));
			assertTrue(allJobs.contains(new CrawlJob(3, new URL("http", "example.kings.edu", "/scripts/public/"))));
			assertTrue(allJobs.contains(new CrawlJob(4, new URL("http", "example.kings.edu", "/scripts/public/foo.html"))));
		}
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#cancelCrawlingHtmlFile(CrawlJob)} works correctly.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException If there is a problem with the test itself.
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 */
	@Test
	public void testCancelCrawlingHtmlFile() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/')");
			
			CrawlJob oldJob = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			
			dataSource.cancelCrawlingHtmlFile(oldJob);
			
			Set<CrawlJob> allJobs = new HashSet<>();
			try(ResultSet resultsAllJobs = stmt.executeQuery("SELECT url_id, protocol, host_name, path FROM url NATURAL JOIN host WHERE when_crawled IS NULL");) {
				while(resultsAllJobs.next()) {
					allJobs.add(new CrawlJob(resultsAllJobs.getInt(1), new URL(resultsAllJobs.getString(2), resultsAllJobs.getString(3), resultsAllJobs.getString(4))));
				}
			}
			assertEquals(allJobs.size(), 1);
			assertTrue(allJobs.contains(new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"))));
		}
	}

	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} works when called sequentially.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException If there is a problem with the test itself.
	 * @throws DataSourceException If there is a problem interacting with the PGCrawlingDataSource.
	 */
	@Test
	public void testFinishCrawlingHtmlFileDouble() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/admissions/')");
			
			CrawlJob firstJob = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(firstJob, newUrls, "This is some content.");
			assertEquals(newJobs.size(), 0);
			CrawlJob secondJob = new CrawlJob(2, new URL("http", "example.kings.edu", "/admissions/"));
			newJobs = dataSource.finishCrawlingHtmlFile(secondJob, newUrls, "More content.");
			assertEquals(newJobs.size(), 0);
		}		
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} handles malformed document contents correctly.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException If there is an error in the test itself.
	 * @throws DataSourceException If there is a problem communicating with the PGCrawlingDataSource.
	 */
	@Test
	public void testFinishCrawlingHtmlFileBadContent() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			
			CrawlJob job = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(job, newUrls, "Hello \0 World!");
			assertEquals(newJobs.size(), 0);
			try(ResultSet resultsGetContent = stmt.executeQuery("SELECT content FROM document");) {
				assertFalse(resultsGetContent.next());
			}
		}
	}
	
	/**
	 * Tests that {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)} does case-insensitive matching of the extension blackliist.
	 * 
	 * @throws SQLException If there is a problem communicating with the database.
	 * @throws MalformedURLException If there is an error in the test itself.
	 * @throws DataSourceException If there is a problem communicating with the PGCrawlingDataSource.
	 */
	@Test
	public void testExtensionBlacklistCaseInsensitive() throws SQLException, MalformedURLException, DataSourceException {
		try(Statement stmt = connection.createStatement();) {
			stmt.executeUpdate("INSERT INTO host VALUES (DEFAULT, 'example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/')");
			stmt.executeUpdate("INSERT INTO extension_blacklist VALUES ('.jpg')");
			stmt.executeUpdate("INSERT INTO host_whitelist VALUES ('example.kings.edu')");
			stmt.executeUpdate("INSERT INTO url VALUES (DEFAULT, 'http', 1, '/robots.txt', NOW())");
			stmt.executeUpdate("INSERT INTO robots_txt_rule VALUES ('http', 1, '/', true)");
			
			CrawlJob job = new CrawlJob(1, new URL("http", "example.kings.edu", "/"));
			Set<URL> newUrls = new HashSet<>();
			newUrls.add(new URL("http://example.kings.edu/picture.JPG"));
			newUrls.add(new URL("http://example.kings.edu/image.jpg"));
			newUrls.add(new URL("http://example.kings.edu/document.html"));
			Set<CrawlJob> newJobs = dataSource.finishCrawlingHtmlFile(job, newUrls, "<html></html>");
			assertEquals(1, newJobs.size());
			assertTrue(newJobs.contains(new CrawlJob(3, new URL("http://example.kings.edu/document.html"))));
		}
	}
}
