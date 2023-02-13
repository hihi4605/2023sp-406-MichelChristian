-- The initial SQL file that creates the database objects needed for crawling the web.
-- You should always execute this script with your search path set to the schema you want these documents to be in.
-- Author: Chad Hogg


-- A computer from which we would like to crawl documents.
CREATE TABLE host (
  host_id SERIAL,                   -- Just an artificial key to make other tables take up less space.
  host_name TEXT NOT NULL,  		-- The fully-qualified domain name or IP address of this host.
  PRIMARY KEY (host_id),
  UNIQUE (host_name)
);

-- The URL of a document on the web.
-- Note that alternative ports, authentication, and fragments are not supported.
CREATE TABLE url (
  url_id SERIAL,                    -- Just an artificial key to make other tables take up less space.
  protocol VARCHAR(5) NOT NULL,     -- The protocol / scheme of this URL.
  host_id INTEGER NOT NULL,         -- The ID of the host of this URL.
  path TEXT NOT NULL,               -- The path of this URL.
  when_crawled TIMESTAMP,           -- When (if ever) this URL was crawled.
  PRIMARY KEY (url_id),
  UNIQUE (protocol, host_id, path),
  FOREIGN KEY (host_id) REFERENCES host,
  CHECK (protocol = 'http' OR
    protocol = 'https')
);

-- A web document.
CREATE TABLE document (
  url_id INT,                       -- The ID of the URL at which this document was found.
  title TEXT,                       -- The title of the document.  This is a part of the content that we will want easy access to.
  content TEXT,                     -- The actual HTML of the document.
  pagerank DOUBLE PRECISION,				-- The Pagerank value of the document.
  word_count INTEGER,				-- The number of words in the document (after stopping).
  anchor_count INTEGER,				-- The number of words in the anchor text of documents that link to this.
  snippet TEXT,						-- The generic snippet to show for this document.
  PRIMARY KEY (url_id),
  FOREIGN KEY (url_id) REFERENCES url ON DELETE CASCADE
);

-- A list of hosts that we want to crawl.
-- Note that this is actually storing host suffixes, so that if 'kings.edu' is on this list, then www.kings.edu and departments.kings.edu are also acceptable.
-- This is used to limit which hosts we are willing to crawl.
CREATE TABLE host_whitelist (
  host_suffix TEXT,
  PRIMARY KEY (host_suffix)
);

-- A list of hosts that we do *not* want to crawl.
-- This is used for hosts that store problematic documents.
CREATE TABLE host_blacklist (
  host_name TEXT,
  explanation TEXT,
  PRIMARY KEY (host_name)
);

-- A list of file extensions that we will not crawl because it is safe to assume that they are not HTML documents.
CREATE TABLE extension_blacklist (
  extension VARCHAR(5),
  PRIMARY KEY (extension)
);

-- A list of the rules we have read from robots.txt files.
CREATE TABLE robots_txt_rule (
  protocol VARCHAR(5),              -- The protocol of URLs to which this rule applies.
  host_id INT,                      -- The ID of the host to which this rule applies.
  path_prefix TEXT,                 -- The prefix of this rule.
  directive BOOLEAN NOT NULL,       -- Whether this rule is to Allow (true) or Disallow (false).
  PRIMARY KEY (protocol, host_id, path_prefix),
  FOREIGN KEY (host_id) REFERENCES host
);

