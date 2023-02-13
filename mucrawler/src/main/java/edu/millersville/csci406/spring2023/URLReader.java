package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Scanner;

import org.jsoup.nodes.Document;

/**
 * An interface for any class that can open files specified by URLs.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public interface URLReader {

	/**
	 * Gets a Scanner that is prepared to read the robots.txt file at a given URL.
	 * 
	 * @param url The URL of the robots.txt file requested.
	 * @return A Scanner that is prepared to read the robots.txt file requested, or null if it cannot be opened.
	 */
	public Scanner readRobotsTxtFile(URL url);

	/**
	 * Gets a parsed Document representing an HTML file at a given URL.
	 * Returns null if the file does not exist or is not an HTML file.
	 * 
	 * @param url The URL of the HTML file requested.
	 * @return A Document representing the HTML file, or null.
	 */
	public Document readHTMLFile(URL url);
}
