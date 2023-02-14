package edu.millersville.csci406.spring2023;

public class CrawlerMain {

    long crawlDelay = 10000;
 
        PGCrawlingDataSource source = new PGCrawlingDataSource(null, null, null, null, null);
        MyCrawlController controller = new MyCrawlController(source,crawlDelay);
        NetworkURLReader reader = new NetworkURLReader();
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
        one.join();
        two.join();
        three.join();
        four.join();
        
    }
   

