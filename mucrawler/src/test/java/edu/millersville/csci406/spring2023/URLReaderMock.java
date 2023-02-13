package edu.millersville.csci406.spring2023;

import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A mock implementation of URLReader for use when testing CrawlWorkerThread.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class URLReaderMock implements URLReader {
	
	/** A Map of URL to a String containing what we will claim to be the contents of the file at that URL. */
	private Map<URL, String> fileContents;
	
	/**
	 * Constructs a URLReaderMock.
	 * 
	 * @param fileContents A Map of URL to file contents.
	 */
	public URLReaderMock(Map<URL, String> fileContents) {
		this.fileContents = fileContents;
	}

	@Override
	public Scanner readRobotsTxtFile(URL url) {
		String contents = fileContents.get(url);
		Scanner returnValue = null;
		if(contents != null) {
			returnValue = new Scanner(contents);
		}
		return returnValue;
	}

	@Override
	public Document readHTMLFile(URL url) {
		String contents = fileContents.get(url);
		Document returnValue = null;
		if(contents != null) {
			returnValue = Jsoup.parse(contents, url.toString());
		}
		return returnValue;
	}

}
