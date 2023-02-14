package edu.millersville.csci406.spring2023;

import java.lang.Thread;
import java.util.logging.*;

/**
 * Name: Christian Michel, Chad Hogg
 * Data: 2/10/23
 * Assignment: STAGE 01: FINISHING THE CRAWLER
 * Proffessor: Chad Hogg
 * Description: On this stage of the crawler we implement URLReader and main. Main initiates 10 
 * CrawlWorkerThreads to crawl through our datasource.
 */
public class CrawlerMain {

    public static void main(String[] args) {
      try {
        long crawlDelay = 10000;
        Logger.getLogger("").setLevel(Level.INFO);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);    
    
        PGCrawlingDataSource source = new PGCrawlingDataSource("localhost", "search", "search", "muuuugle", "real");
        MyCrawlController controller = new MyCrawlController(source,crawlDelay);
        NetworkURLReader reader = new NetworkURLReader();
        startThreads(controller, reader);
        
        
      } catch (Exception e) {
        Logger.getLogger(e.toString());
      }
        
    }



    public static void startThreads(CrawlController controller, URLReader reader)
    {
        Thread one = new Thread(new CrawlWorkerThread(controller, reader));
        Thread two = new Thread(new CrawlWorkerThread(controller, reader));
        Thread three = new Thread(new CrawlWorkerThread(controller, reader));
        Thread four = new Thread(new CrawlWorkerThread(controller, reader));
        Thread five = new Thread(new CrawlWorkerThread(controller, reader));
        Thread six = new Thread(new CrawlWorkerThread(controller, reader));
        Thread seven = new Thread(new CrawlWorkerThread(controller, reader));
        Thread eight = new Thread(new CrawlWorkerThread(controller, reader));
        Thread nine = new Thread(new CrawlWorkerThread(controller, reader));
        Thread ten = new Thread(new CrawlWorkerThread(controller, reader));
        one.start();
        two.start();
        three.start();
        four.start();
        five.start();
        six.start();
        seven.start();
        eight.start();
        nine.start();
        ten.start();
        try {
            one.join();
            two.join();
            three.join();
            four.join();
            five.join();
            six.join();
            seven.join();
            eight.join();
            nine.join();
            ten.join();
        } catch (InterruptedException e) {
      
            e.printStackTrace();
        }
    }
   

}