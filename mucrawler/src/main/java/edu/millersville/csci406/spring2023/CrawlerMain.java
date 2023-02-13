package edu.millersville.csci406.spring2023;

public class CrawlerMain {
    PGCrawlingDataSource source = new PGCrawlingDataSource(null, null, null, null, null);
    MyCrawlController controller = new MyCrawlController(source, 1000);
    NetworkURLReader reader = new NetworkURLReader();
    Thread one = new Thread(new CrawlWorkerThread(controller, reader));

}
