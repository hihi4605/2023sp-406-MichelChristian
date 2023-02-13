-- An SQL file that will reset the database to start the crawling process.
-- You should always execute this script with your search path set to the schema you want these documents to be in.
-- Author: Chad Hogg


-- Remove all existing data.
TRUNCATE robots_txt_rule, extension_blacklist, host_blacklist, host_whitelist, document, url, host RESTART IDENTITY;

-- The sites that we want to crawl.
INSERT INTO host_whitelist VALUES ( 'millersville.edu' );
INSERT INTO host_whitelist VALUES ( 'fandm.edu' );
INSERT INTO host_whitelist VALUES ( 'etown.edu' );
INSERT INTO host_whitelist VALUES ( 'ycp.edu' );
INSERT INTO host_whitelist VALUES ( 'stevenscollege.edu' );
INSERT INTO host_whitelist VALUES ( 'lvc.edu' );
INSERT INTO host_whitelist VALUES ( 'harrisburgu.edu' );
INSERT INTO host_whitelist VALUES ( 'wcupa.edu' );
INSERT INTO host_whitelist VALUES ( 'cheyney.edu' );
INSERT INTO host_whitelist VALUES ( 'valleyforge.edu' );
INSERT INTO host_whitelist VALUES ( 'lincoln.edu' );
INSERT INTO host_whitelist VALUES ( 'immaculata.edu' );
INSERT INTO host_whitelist VALUES ( 'lbc.edu' );
INSERT INTO host_whitelist VALUES ( 'albright.edu' );
INSERT INTO host_whitelist VALUES ( 'alvernia.edu' );
INSERT INTO host_whitelist VALUES ( 'kutztown.edu' );

-- The hosts that we definitely do not want to crawl.

-- The extensions of files that we definitely do not want to crawl.
INSERT INTO extension_blacklist VALUES ( '.jpg' );
INSERT INTO extension_blacklist VALUES ( '.png' );
INSERT INTO extension_blacklist VALUES ( '.pdf' );
INSERT INTO extension_blacklist VALUES ( '.doc' );
INSERT INTO extension_blacklist VALUES ( '.docx' );
INSERT INTO extension_blacklist VALUES ( '.ppt' );
INSERT INTO extension_blacklist VALUES ( '.xlsx' );
INSERT INTO extension_blacklist VALUES ( '.wav' );
INSERT INTO extension_blacklist VALUES ( '.mp3' );
INSERT INTO extension_blacklist VALUES ( '.css' );
INSERT INTO extension_blacklist VALUES ( '.txt' );
INSERT INTO extension_blacklist VALUES ( '.gif' );
INSERT INTO extension_blacklist VALUES ( '.pptx' );
INSERT INTO extension_blacklist VALUES ( '.xls' );
INSERT INTO extension_blacklist VALUES ( '.swf' );
INSERT INTO extension_blacklist VALUES ( '.wmv' );

-- The initial documents that we would like to crawl.
INSERT INTO host VALUES ( DEFAULT, 'www.millersville.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.fandm.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.etown.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.ycp.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.stevenscollege.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.lvc.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.harrisburgu.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.wcupa.edu' );
INSERT INTO host VALUES ( DEFAULT, 'cheyney.edu' );
INSERT INTO host VALUES ( DEFAULT, 'valleyforge.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.lincoln.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.immaculata.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.lbc.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.albright.edu' );
INSERT INTO host VALUES ( DEFAULT, 'www.alvernia.edu' );
INSERT INTO host VALUES ( DEFAULT, 'kutztown.edu' );

INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.millersville.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.millersville.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.fandm.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.fandm.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.etown.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.etown.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.ycp.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.ycp.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.stevenscollege.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.stevenscollege.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lvc.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lvc.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.harrisburgu.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.harrisburgu.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.wcupa.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.wcupa.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'cheyney.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'cheyney.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'valleyforge.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'valleyforge.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lincoln.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lincoln.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.immaculata.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.immaculata.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lbc.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.lbc.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.albright.edu'), '/home/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.albright.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.alvernia.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'www.alvernia.edu'), '/robots.txt' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'kutztown.edu'), '/' );
INSERT INTO url VALUES ( DEFAULT, 'https', (SELECT host_id FROM host WHERE host_name = 'kutztown.edu'), '/robots.txt' );

