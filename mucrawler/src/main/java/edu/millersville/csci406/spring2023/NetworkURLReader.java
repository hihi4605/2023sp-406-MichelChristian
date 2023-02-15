package edu.millersville.csci406.spring2023;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * An implementation of URLReader. Takes a URL and returns a document from said URL. If a connection
 * cannot be established to the URL null is returned.
 */
public class NetworkURLReader implements URLReader {

    @Override
    public Scanner readRobotsTxtFile(URL url) {
        try {
            Scanner sc = new Scanner(new BufferedInputStream(url.openStream()));
            return sc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param url The url of the HTML file we want to establish connection to.
     * @return A document made from the url, or null if the document is unreadable or a connection
     * could not be established. 
     */
    @Override
    public Document readHTMLFile(URL url) {
        try {
            URLConnection con = url.openConnection();

            if (con.getContentType() == null || !con.getContentType().contains("html")) {
                return null;
            }
            Document doc = Jsoup.connect(url.toString()).get();
            return doc;
        } catch (IOException e) {
            return null;
        }

    }
}