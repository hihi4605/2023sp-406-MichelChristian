package edu.millersville.csci406.spring2023;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A thread that repeatedly gets CrawlJobs from a CrawlController and completes
 * them.
 * 
 * @author Chad Hogg
 * @version 2023-01-19
 */
public class CrawlWorkerThread implements Runnable {

    /** A Logger. */
    private static Logger theLogger = Logger.getLogger(CrawlWorkerThread.class.getName());

    /**
     * A CrawlController from whom this will get CrawlJobs and to whom it will
     * provide notifications when CrawlJobs are completed.
     */
    private CrawlController controller;

    /** A URLReader from which we can open the files at URLs. */
    private URLReader urlReader;

    /**
     * Constructs a new CrawlWorkerThread.
     * 
     * @param controller A CrawlController for the new CrawlWorkerThread.
     * @param urlReader  A URLReader for the new CrawlWorkerThread.
     */
    public CrawlWorkerThread(CrawlController controller, URLReader urlReader) {
        this.controller = controller;
        this.urlReader = urlReader;
    }

    /**
     * Processes a CrawlJob that represents a robots.txt file, extracting rules and
     * returning them to the CrawlController.
     * 
     * @param job The CrawlJob to process, which must be for a robots.txt file.
     * @throws DataSourceException If there is a problem accessing the DataSource.
     */
    private void processRobotsTxtFile(CrawlJob job) throws DataSourceException {
        Set<RobotsRule> rules = new HashSet<>();
        try (Scanner input = urlReader.readRobotsTxtFile(job.getURL());) {

            if (input != null) {
                while (input.hasNextLine()) {
                    String line = input.nextLine();
                    if (line.equalsIgnoreCase("User-agent: *")) {
                        parseRobotsRecord(job.getURL().getProtocol(), job.getURL().getHost(), rules, input);
                    }
                }
            }
        }

        if (rules.isEmpty()) {
            rules.add(new RobotsRule(job.getURL().getProtocol(), job.getURL().getHost(), "/", true));
        }
        controller.finishRobots(job, rules);
    }

    /**
     * Processes a CrawlJob that represents an HTML file.
     * If it really is an HTML file, extracts relevant links and content from it and
     * sends them to the CrawlController.
     * If it is not an HTML file / cannot be opened, cancels it to the
     * CrawlController.
     * 
     * @param job The CrawlJob to process.
     * @throws DataSourceException If there is a problem accessing the DataSource.
     */
    private void processHTMLFile(CrawlJob job) throws DataSourceException {
        Document document = urlReader.readHTMLFile(job.getURL());
        if (document != null) {
            controller.finishHtml(job, extractLinks(document), document.outerHtml());
        } else {
            controller.cancelHtml(job);
        }
    }

    /**
     * Parses an individual record from a robots.txt file.
     * 
     * @param protocol Either "http" or "https".
     * @param hostName The name of the host from which the robots.txt file
     *                 originated.
     * @param rules    A set in which to insert the rules that we find.
     * @param input    A Scanner through the file, which should be positioned on the
     *                 second row of the record.
     */
    private void parseRobotsRecord(String protocol, String hostName, Set<RobotsRule> rules, Scanner input) {
        boolean readEmptyLine = false;
        while (input.hasNextLine() && !readEmptyLine) {
            String line = input.nextLine();
            if (line.equals("")) {
                readEmptyLine = true;
            } else {
                if (line.startsWith("Disallow:")) {
                    String prefix = line.substring(9);
                    if (prefix.indexOf('#') != -1) {
                        prefix = prefix.substring(0, prefix.indexOf('#')).trim();
                    }
                    prefix = prefix.trim();
                    if (!prefix.isEmpty()) {
                        rules.add(new RobotsRule(protocol, hostName, prefix, false));
                    } else {
                        rules.add(new RobotsRule(protocol, hostName, "/", true));
                    }
                } else if (line.startsWith("Allow:")) {
                    String prefix = line.substring(6);
                    if (prefix.indexOf('#') != -1) {
                        prefix = prefix.substring(0, prefix.indexOf('#')).trim();
                    }
                    prefix = prefix.trim();
                    if (!prefix.isEmpty()) {
                        rules.add(new RobotsRule(protocol, hostName, prefix, true));
                    }
                } else if (line.startsWith("Crawl-delay")) {
                    // We are ignoring this request for now, since it is nonstandard.
                } else if (line.startsWith("Sitemap")) {
                    // We are ignoring sitemaps for now, since they are nonstandard.
                } else if (line.startsWith("#")) {
                    // These are comments.
                } else {
                    theLogger.log(Level.INFO, "Found unparseable line \"" + line + "\" in " + protocol + "://"
                            + hostName + "/robots.txt");
                }
            }
        }
    }

    /**
     * Pulls all of the links out of an HTML Document.
     * Ignores any links with non-http/https protocols.
     * 
     * @param document The current Document.
     * @return A set of URLs of links found within the Document.
     */
    private Set<URL> extractLinks(Document document) {
        Set<URL> urls = new HashSet<>();
        Elements links = document.select("a[href]");

        for (Element link : links) {
            URL newURL;
            try {
                newURL = new URL(link.attr("abs:href"));
                if (newURL.getProtocol().equalsIgnoreCase("http") || newURL.getProtocol().equalsIgnoreCase("https")) {
                    if (newURL.getPort() == -1) {
                        if (!newURL.getFile().startsWith("/")) {
                            newURL = new URL(newURL.getProtocol(), newURL.getHost(), "/" + newURL.getFile());
                        }
                        urls.add(newURL);
                    }
                }
            } catch (MalformedURLException e) {
                // Fine, we will just ignore that link.
            }
        }
        return urls;
    }

    @Override
    public void run() {
        try {
            CrawlJob currentJob = controller.getJob();
            while (currentJob != null) {
                theLogger.log(Level.INFO, "Processing " + currentJob);
                if (currentJob.getURL().getFile().equalsIgnoreCase("/robots.txt")) {
                    processRobotsTxtFile(currentJob);
                } else {
                    processHTMLFile(currentJob);
                }
                currentJob = controller.getJob();
            }
        } catch (DataSourceException exception) {
            theLogger.log(Level.SEVERE, "Thread exiting due to exception.", exception.getCause());
        }
    }

}
