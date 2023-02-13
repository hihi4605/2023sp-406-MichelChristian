package edu.millersville.csci406.spring2023;

/**
 * Configuration of where databases and files are stored.
 * 
 * @author Chad Hogg
 * @version 2023-02-02
 */
public class ConfigurationDefaults {

	/** The FQDN of the computer on which the database is located. */
	public static final String DEFAULT_HOST_NAME = "localhost";
	/** The database in which the search engine's information is stored. */
	public static final String DEFAULT_DATABASE_NAME = "search";
	/** The username with which we connect to the database. */
	public static final String DEFAULT_USER_NAME = "search";
	/** The password with which we connect to the database. */
	public static final String DEFAULT_PASSWORD = "muuugle";
	/** The schema in which all tables should be found. */
	public static final String DEFAULT_SCHEMA = "real";

}
