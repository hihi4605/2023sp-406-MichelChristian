package edu.millersville.csci406.spring2023;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * A DataSource implementation that reads all data from a PostgreSQL database.
 * 
 * @author Chad Hogg
 * @version 2023-02-02
 */
public class PGCrawlingDataSource implements CrawlingDataSource, AutoCloseable {

	/** A Logger. */
	private static Logger theLogger = Logger.getLogger(PGCrawlingDataSource.class.getName());

	/**
	 * An enumeration of all database commands used by this DatabaseDataSource.
	 * 
	 * @author Chad Hogg
	 * @version 2018-01-27
	 */
	private static enum DatabaseCommand {

		/** Gets a list of all URLs that have not yet been crawled. */
		GET_URLS_TO_CRAWL("SELECT url_id, protocol, host_name, path FROM url NATURAL JOIN host WHERE when_crawled IS NULL"),
		/** Gets the longest matching robots.txt rule for a URL. */
		GET_LONGEST_ROBOTS_RULE("SELECT path_prefix, directive FROM robots_txt_rule NATURAL JOIN host WHERE protocol = ? AND host_name = ? AND ? LIKE path_prefix || '%' ORDER BY CHAR_LENGTH(path_prefix) DESC LIMIT 1"),
		/** Adds a new rule from a robots.txt file. */
		ADD_ROBOTS_TXT_RULE("INSERT INTO robots_txt_rule (protocol, host_id, path_prefix, directive) VALUES (?, ?, ?, ?)"),
		/** Gets the host_id matching a host_name. */
		GET_HOST_ID_FROM_NAME("SELECT host_id FROM host WHERE host_name = ?"),
		/** Gets a list of all URLs that should not be crawled due to robots.txt rules. */
		GET_DISALLOWED_URLS("SELECT url_id, path FROM url WHERE protocol = ? AND host_id = ? AND (SELECT directive FROM robots_txt_rule WHERE protocol = url.protocol AND host_id = url.host_id AND path LIKE path_prefix || '%' ORDER BY CHAR_LENGTH(path_prefix) DESC LIMIT 1) = false"),
		/** Deletes a URL from the collection. */
		DELETE_URL("DELETE FROM url WHERE url_id = ?"),
		/** Adds a new URL to the collection. */
		ADD_URL("INSERT INTO url (url_id, protocol, host_id, path, when_crawled) VALUES (DEFAULT, ?, ?, ?, NULL) RETURNING url_id"),
		/** Gets the ID (if there is one) of a URL. */
		GET_ID_FOR_URL("SELECT url_id FROM url NATURAL JOIN host WHERE protocol = ? AND host_name = ? AND path = ?"),
		/** Gets the set of white-listed host name suffixes. */
		GET_HOST_WHITELIST("SELECT host_suffix FROM host_whitelist"),
		/** Gets the set of black-listed host names. */
		GET_HOST_BLACKLIST("SELECT host_name FROM host_blacklist"),
		/** Gets the set of black-listed file extensions. */
		GET_EXTENSION_BLACKLIST("SELECT extension FROM extension_blacklist"),
		/** Creates a new document for a certain URL. */
		CREATE_DOCUMENT("INSERT INTO document (url_id, content) VALUES (?, ?)"),
		/** Creates a new host and returns its ID. */
		ADD_HOST("INSERT INTO host (host_id, host_name) VALUES (DEFAULT, ?) RETURNING host_id"),
		/** Update the crawl time on a URL. */
		SET_CRAWL_TIME("UPDATE url SET when_crawled = NOW() WHERE url_id = ?"),
		;
		
		/** The actual text used to create a PreparedStatement of this DatabaseCommand. */
		private String commandString;
		
		/**
		 * Constructs a new DatabaseCommand.
		 * 
		 * @param commandString The actual text of the DatabaseCommand.
		 */
		private DatabaseCommand(String commandString) {
			this.commandString = commandString;
		}
		
		/**
		 * Gets the text of this DatabaseCommand.
		 * 
		 * @return The text of this DatabaseCommand.
		 */
		public String getCommandString() {
			return commandString;
		}
	}
	
	/** A connection to the database. */
	private Connection conn;
	
	/** 
	 * A Map of DatabaseCommands to PreparedStatements that will execute those commands.
	 * The contents of this map are lazily instantiated in {@link PGCrawlingDataSource#getStatement(DatabaseCommand)}, because most applications will only use a few but will reuse them many times.
	 */
	private Map<DatabaseCommand, PreparedStatement> statements;
	
	/**
	 * A set of Strings such that the hostname of any URL we crawl must match or end with one of these.
	 * This set is lazily instantiated in {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)}, because applications will either never use it or use it many times.
	 */
	private Set<String> hostWhitelist;

	/**
	 * A set of Strings such that the hostname of any URL we crawl may not exactly match any of these.  
	 * This set is lazily instantiated in {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)}, because applications will either never use it or use it many times.
	 */
	private Set<String> hostBlacklist;

	/**
	 * A set of Strings such that the file extension of any URL we crawl may not exactly match any of these.  
	 * This set is lazily instantiated in {@link PGCrawlingDataSource#finishCrawlingHtmlFile(CrawlJob, Set, String)}, because applications will either never use it or use it many times.
	 */
	private Set<String> extensionBlacklist;

	/**
	 * Constructs a new DatabaseDataSource.
	 * 
	 * @param hostName The name of the host to which we should connect.
	 * @param database The database to which we should connect.
	 * @param user The name of the user with which we should connect.
	 * @param password The password with which we should connect.
	 * @param schema The database schema is which we should work.
	 * @throws SQLException If we cannot create a connection or set a schema.
	 */
	public PGCrawlingDataSource(String hostName, String database, String user, String password, String schema) throws SQLException {
		conn = DriverManager.getConnection("jdbc:postgresql://" + hostName + "/" + database, user, password);
		conn.setSchema(schema);
		statements = new HashMap<>();
		hostWhitelist = null;
		hostBlacklist = null;
		extensionBlacklist = null;
	}
	
	@Override
	public void close() {
		try {
			for(PreparedStatement stmt : statements.values()) {
				stmt.close();
			}
			conn.close();
		} catch (SQLException e) {
			// I can't think of a way to get coverage of this line other than to pull my network cable in the middle of the method.
			// Even if I were to create a mock PreparedStatement class, how would I use it?  The object is entirely contained within this class.
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the PreparedStatement to use for a Query, creating it if necessary and caching it for future use.
	 * 
	 * @param command The command whose statement is needed.
	 * @return A PreparedStatement for the specified DatabaseCommand.
	 * @throws SQLException If a PreparedStatement cannot be created.
	 */
	private synchronized PreparedStatement getStatement(DatabaseCommand command) throws SQLException {
		if(!statements.containsKey(command)) {
			statements.put(command, conn.prepareStatement(command.getCommandString()));
		}
		return statements.get(command);
	}


	@Override
	public Set<CrawlJob> getURLsToCrawl() throws DataSourceException {
		Set<CrawlJob> urls = new HashSet<>();
		try {
			PreparedStatement stmt = getStatement(DatabaseCommand.GET_URLS_TO_CRAWL);
			try (ResultSet results = stmt.executeQuery();) {
				while(results.next()) {
					urls.add(new CrawlJob(results.getInt(1), new URL(results.getString(2), results.getString(3), results.getString(4))));
				}
			}
		}
		catch(SQLException | MalformedURLException exception) {
			// Similarly, I don't know how to get coverage of this line.
			throw new DataSourceException(exception);
		}
		return urls;
	}

	@Override
	public Set<CrawlJob> finishCrawlingRobotsFile(CrawlJob job, Set<RobotsRule> newRules) throws DataSourceException {
		Set<CrawlJob> deletedJobs = new HashSet<>();
		String protocol = job.getURL().getProtocol();
		String hostName = job.getURL().getHost();
		
		// If there is no root rule, we create one saying that everything is allowed.
		boolean foundARootRule = false;
		Iterator<RobotsRule> iter = newRules.iterator();
		while(iter.hasNext() && !foundARootRule) {
			RobotsRule rule = iter.next();
			if(rule.getPathPrefix().equals("/")) {
				foundARootRule = true;
			}
		}
		if(!foundARootRule) {
			newRules.add(new RobotsRule(protocol, hostName, "/", true));
		}
		
		try {
			// First, just get the ID of the affected host, since many other tables will use it.
			PreparedStatement stmtGetHostId = getStatement(DatabaseCommand.GET_HOST_ID_FROM_NAME);
			stmtGetHostId.setString(1, hostName);
			int hostId;
			try (ResultSet resultsGetHostId = stmtGetHostId.executeQuery();) {
				resultsGetHostId.next();
				hostId = resultsGetHostId.getInt(1);
			}
			// Now go through and add all of the new robots.txt rules to the database.
			PreparedStatement stmtAddRule = getStatement(DatabaseCommand.ADD_ROBOTS_TXT_RULE);
			for(RobotsRule newRule : newRules) {
				stmtAddRule.setString(1, protocol);
				stmtAddRule.setInt(2, hostId);
				stmtAddRule.setString(3, newRule.getPathPrefix());
				stmtAddRule.setBoolean(4, newRule.isAllowed());
				stmtAddRule.executeUpdate();
			}
			// Now record and delete every existing URL that is disallowed by the new rules we just added.
			PreparedStatement stmtGetDisallowedUrls = getStatement(DatabaseCommand.GET_DISALLOWED_URLS);
			stmtGetDisallowedUrls.setString(1, protocol);
			stmtGetDisallowedUrls.setInt(2, hostId);
			PreparedStatement stmtDeleteUrl = getStatement(DatabaseCommand.DELETE_URL);
			try (ResultSet resultsGetDisallowedUrls = stmtGetDisallowedUrls.executeQuery();) {
				while(resultsGetDisallowedUrls.next()) {
					deletedJobs.add(new CrawlJob(resultsGetDisallowedUrls.getInt(1), new URL(protocol, hostName, resultsGetDisallowedUrls.getString(2))));
					stmtDeleteUrl.setInt(1, resultsGetDisallowedUrls.getInt(1));
					stmtDeleteUrl.executeUpdate();
				}
			}
			// Now update the crawl time for the robots.txt file itself.
			PreparedStatement stmtSetCrawlTime = getStatement(DatabaseCommand.SET_CRAWL_TIME);
			stmtSetCrawlTime.setInt(1, job.getId());
			stmtSetCrawlTime.executeUpdate();
		}
		catch(SQLException | MalformedURLException exception) {
			throw new DataSourceException(exception);
		}
		return deletedJobs;
	}

	@Override
	public Set<CrawlJob> finishCrawlingHtmlFile(CrawlJob job, Set<URL> newUrls, String content) throws DataSourceException {
		Set<CrawlJob> newJobs = new HashSet<>();
		try {
			// We just want to get the whitelist once, to save time.
			if(hostWhitelist == null) {
				PreparedStatement stmtGetHostWhitelist = getStatement(DatabaseCommand.GET_HOST_WHITELIST);
				hostWhitelist = new HashSet<>();
				try(ResultSet resultsGetHostWhitelist = stmtGetHostWhitelist.executeQuery();) {
					while(resultsGetHostWhitelist.next()) {
						hostWhitelist.add(resultsGetHostWhitelist.getString(1));
					}
				}				
			}
			if(hostBlacklist == null) {
				PreparedStatement stmtGetHostBlacklist = getStatement(DatabaseCommand.GET_HOST_BLACKLIST);
				hostBlacklist = new HashSet<>();
				try(ResultSet resultsGetHostBlacklist = stmtGetHostBlacklist.executeQuery();) {
					while(resultsGetHostBlacklist.next()) {
						hostBlacklist.add(resultsGetHostBlacklist.getString(1));
					}
				}
			}
			if(extensionBlacklist == null) {
				PreparedStatement stmtGetExtensionBlacklist = getStatement(DatabaseCommand.GET_EXTENSION_BLACKLIST);
				extensionBlacklist = new HashSet<>();
				try(ResultSet resultsGetExtensionBlacklist = stmtGetExtensionBlacklist.executeQuery();) {
					while(resultsGetExtensionBlacklist.next()) {
						extensionBlacklist.add(resultsGetExtensionBlacklist.getString(1));
					}
				}
			}
			
			PreparedStatement stmtGetLongestRobotsRule = getStatement(DatabaseCommand.GET_LONGEST_ROBOTS_RULE);
			PreparedStatement stmtAddUrl = getStatement(DatabaseCommand.ADD_URL);
			PreparedStatement stmtGetIdForUrl = getStatement(DatabaseCommand.GET_ID_FOR_URL);
			PreparedStatement stmtCreateDocument = getStatement(DatabaseCommand.CREATE_DOCUMENT);
			PreparedStatement stmtGetHostIdFromName = getStatement(DatabaseCommand.GET_HOST_ID_FROM_NAME);
			PreparedStatement stmtAddHost = getStatement(DatabaseCommand.ADD_HOST);
			PreparedStatement stmtSetCrawlTime = getStatement(DatabaseCommand.SET_CRAWL_TIME);
			
			for(URL newUrl : newUrls) {
				boolean addUrl = true;
				boolean addRobots = false;
				
				// We only allow URLs whose protocols are "http" or "https".
//				if(!(newUrl.getProtocol().equals("http") || newUrl.getProtocol().equals("https"))) {
//					addUrl = false;
//				}

				// We are only interested in adding this URL if it does not already exist.
				if(addUrl) {
					stmtGetIdForUrl.setString(1, newUrl.getProtocol());
					stmtGetIdForUrl.setString(2, newUrl.getHost());
					stmtGetIdForUrl.setString(3, newUrl.getFile());
					try(ResultSet resultsGetIdForUrl = stmtGetIdForUrl.executeQuery();) {
						if(resultsGetIdForUrl.isBeforeFirst()) {
							addUrl = false;
						}
					}
				}
				
				// We will only add this URL if its host appears on the whitelist.
				if(addUrl) {
					boolean foundAMatch = false;
					Iterator<String> iter = hostWhitelist.iterator();
					while(!foundAMatch && iter.hasNext()) {
						String hostSuffix = iter.next();
						if(newUrl.getHost().equals(hostSuffix) || newUrl.getHost().endsWith("." + hostSuffix)) {
							foundAMatch = true;
						}
					}
					addUrl = foundAMatch;
				}
				
				// We will only add this URL if its host does not appear on the blacklist.
				if(addUrl) {
					if(hostBlacklist.contains(newUrl.getHost())) {
						addUrl = false;
					}
				}
				
				// We will only add this URL if its extension does not appear on the blacklist.
				if(addUrl) {
					boolean foundAMatch = false;
					Iterator<String> iter = extensionBlacklist.iterator();
					while(!foundAMatch && iter.hasNext()) {
						String extension = iter.next();
						if(newUrl.getFile().toLowerCase().endsWith(extension)) {
							foundAMatch = true;
						}
					}
					addUrl = !foundAMatch;
				}

				// We will only add this URL if the longest matching robots.txt rule allows it or there are no robots.txt rules yet.
				if(addUrl) {
					stmtGetLongestRobotsRule.setString(1, newUrl.getProtocol());
					stmtGetLongestRobotsRule.setString(2, newUrl.getHost());
					stmtGetLongestRobotsRule.setString(3, newUrl.getFile());
					try(ResultSet resultsGetLongestRobotsRule = stmtGetLongestRobotsRule.executeQuery();) {
						if(resultsGetLongestRobotsRule.isBeforeFirst()) {
							resultsGetLongestRobotsRule.next();
							if(!resultsGetLongestRobotsRule.getBoolean(2)) {
								addUrl = false;
								theLogger.log(Level.INFO, "Disallowing " + newUrl + " because the longest matching rule was " + resultsGetLongestRobotsRule.getString(1));
							}
							else {
								theLogger.log(Level.INFO, "Adding " + newUrl + " because the longest matching rule was " + resultsGetLongestRobotsRule.getString(1));
							}
						}
						else {
							// If there are no rules and no URL for the robots.txt file, we need to add one.
							stmtGetIdForUrl.setString(1, newUrl.getProtocol());
							stmtGetIdForUrl.setString(2, newUrl.getHost());
							stmtGetIdForUrl.setString(3, "/robots.txt");
							try(ResultSet resultsGetIdForUrl = stmtGetIdForUrl.executeQuery();) {
								if(!resultsGetIdForUrl.isBeforeFirst()) {
									addRobots = true;
									theLogger.log(Level.INFO, "Adding a robots.txt file because there was no longest matching rule for " + newUrl);
								}
							}
						}
					}
				}

				// Actually add the URL.
				if(addUrl) {
					int hostId;
					stmtGetHostIdFromName.setString(1, newUrl.getHost());
					try(ResultSet resultsGetHostIdFromName = stmtGetHostIdFromName.executeQuery();) {
						if(resultsGetHostIdFromName.next()) {
							hostId = resultsGetHostIdFromName.getInt(1);
						}
						else {
							stmtAddHost.setString(1, newUrl.getHost());
							try(ResultSet resultsAddHost = stmtAddHost.executeQuery();) {
								resultsAddHost.next();
								hostId = resultsAddHost.getInt(1);
							}
						}
					}
					stmtAddUrl.setString(1, newUrl.getProtocol());
					stmtAddUrl.setInt(2, hostId);
					stmtAddUrl.setString(3, newUrl.getFile());
					try(ResultSet resultsAddUrl = stmtAddUrl.executeQuery();) {
						resultsAddUrl.next();
						newJobs.add(new CrawlJob(resultsAddUrl.getInt(1), newUrl));
					}
					
					// Actually add the robots.txt file.
					if(addRobots) {
						stmtAddUrl.setString(1, newUrl.getProtocol());
						stmtAddUrl.setInt(2, hostId);
						stmtAddUrl.setString(3, "/robots.txt");
						try(ResultSet resultsAddUrl = stmtAddUrl.executeQuery();) {
							resultsAddUrl.next();
							newJobs.add(new CrawlJob(resultsAddUrl.getInt(1), new URL(newUrl.getProtocol(), newUrl.getHost(), "/robots.txt")));
						}
					}
				}
			}

			// PostgreSQL gets very angry about malformed strings containing the NUL character.
			String revisedContent = content;
			if(content.indexOf('\0') >= 0) {
				revisedContent = "";
			}

			// Actually create the document if there is content.
			if(!revisedContent.equals("")) {
				stmtCreateDocument.setInt(1, job.getId());
				stmtCreateDocument.setString(2, revisedContent);
				stmtCreateDocument.executeUpdate();
			}
			
			// Mark this document as completed.
			stmtSetCrawlTime.setInt(1, job.getId());
			stmtSetCrawlTime.executeUpdate();
		}
		catch(SQLException | MalformedURLException exception) {
			throw new DataSourceException(exception);
		}
		return newJobs;
	}

	@Override
	public void cancelCrawlingHtmlFile(CrawlJob job) throws DataSourceException {
		try {
			PreparedStatement stmtSetCrawlTime = getStatement(DatabaseCommand.SET_CRAWL_TIME);
			stmtSetCrawlTime.setInt(1, job.getId());
			stmtSetCrawlTime.executeUpdate();
		}
		catch(SQLException exception) {
			// Nor do I know how to get coverage of this line ...
			throw new DataSourceException(exception);
		}
	}

}
