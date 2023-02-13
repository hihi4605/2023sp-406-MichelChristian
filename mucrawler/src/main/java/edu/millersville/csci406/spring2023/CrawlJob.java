package edu.millersville.csci406.spring2023;

import java.net.URL;

/**
 * A URL that needs to be crawled, with its associated ID number.
 * 
 * Its natural ordering follows the following preferences, in order:
 *   - robots.txt files come before non-robots.txt files
 *   - http protocols come before non-http protocols
 *   - lower id numbers come before higher id numbers
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class CrawlJob implements Comparable<CrawlJob> {

	/** The unique ID number assigned to this CrawlJob. */
	private final int id;
	/** The URL that needs to be crawled. */
	private final URL url;
	
	/**
	 * Constructs a new CrawlJob.
	 * 
	 * @param id The ID of the new CrawlJob.
	 * @param url The URL of the new CrawlJob.
	 */
	public CrawlJob(int id, URL url) {
		this.id = id;
		this.url = url;
	}
	
	/**
	 * Gets the ID number assigned to this CrawlJob.
	 * 
	 * @return The ID number assigned to this CrawlJob.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the URL that needs to be crawled.
	 * This should be treated as read-only.
	 * 
	 * @return The URL that needs to be crawled.
	 */
	public URL getURL() {
		return url;
	}
	
	@Override
	public int compareTo(CrawlJob arg0) {
		int returnValue;
		if(arg0 == null) {
			throw new NullPointerException();
		}
		else if(equals(arg0)) {
			returnValue = 0;
		}
		else if(url.getFile().equalsIgnoreCase("/robots.txt") && !arg0.url.getFile().equalsIgnoreCase("/robots.txt")) {
			returnValue = -1;
		}
		else if(!url.getFile().equalsIgnoreCase("/robots.txt") && arg0.url.getFile().equalsIgnoreCase("/robots.txt")) {
			returnValue = 1;
		}
		else if(url.getProtocol().equalsIgnoreCase("http") && !arg0.url.getProtocol().equalsIgnoreCase("http")) {
			returnValue = -1;
		}
		else if(!url.getProtocol().equalsIgnoreCase("http") && arg0.url.getProtocol().equalsIgnoreCase("http")) {
			returnValue = 1;
		}
		else if(id < arg0.id){
			returnValue = -1;
		}
		else {
			returnValue = 1;
		}
		return returnValue;
	}
	
	@Override
	public boolean equals(Object arg0) {
		boolean returnValue;
		if(arg0 == null) {
			returnValue = false;
		}
		else if(arg0 instanceof CrawlJob) {
			CrawlJob other = (CrawlJob)arg0;
			if(id == other.id) {
				returnValue = true;
			}
			else {
				returnValue = false;
			}
		}
		else {
			returnValue = false;
		}
		return returnValue;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(id);
		str.append(" ");
		str.append(url);
		return str.toString();
	}
}