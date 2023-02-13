package edu.millersville.csci406.spring2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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