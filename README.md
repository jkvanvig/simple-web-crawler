# simple-web-crawler

Overview
========
This web crawler will crawl only sites within the given domain.  It will ignore external sites and even 
subdomains - i.e., when crawling "example.com", it will ignore "sub.example.com".

This project contains a JettyServer.java file with a main method that starts up a Jetty server with the web crawler 
application running at localhost:8080/simple-web-crawler.  This repository contains the entire eclipse project, and 
requires Gradle for dependency management and assembly.  The easiest way to run it will be to download the contents of the repository and import the existing project into eclipse (with the Gradle plugin), and run or debug JettyServer.java as a java application.

The web crawler is written in Java, with the a very simple front-end that uses the "graphdracula" JavaScript library
to display the two directed graphs - the first represents links between pages, and the second represents the static 
assets that each page depends on.  These are displayed as two separate graphs instead of one because when they are 
combined (like they are in memory on the server), there are generally too many edges for the combined graph to be a 
helpful visual representation.  Another step that was taken to improve the appearance of the graphs is using 
relative urls for pages in the graphs, and filenames as identifiers for the static asset nodes in the 
second graph.

If the graph is too large, a list of the web pages that are in the graph will be displayed instead.

### Data Structures
The basic data structure used are HashMaps.  In order to avoid storing variants of the same key over and over again, 
a very broad HashMap-based tree structure is used, where each level in the tree represents a level in the url - i.e.,
in the url "http://example.com/help/topics/topic-one", there are four parts to the url - "http://example.com", "help", 
"topics", and "topic-one".  The root node would have the base url as the value, and the leaf node for this url would 
be at depth 4.

Links and static assets are stored as object references, so the String identifying each node is only stored once.  
In contrast to the displayed graphs, there is a single graph stored in memory.  Each node has a Set of object references 
representing its outgoing links and static asset dependencies.

### Algorithm
When a page is loaded, the html is parsed and searched for relevent html tags.  "script", "link", and "source" tags 
are read for static assets, and "a" tags are searched for those pointing to links within the domain.  Since the main 
bottleneck is the latency loading web pages, multithreading is used to allow processing to continue while waiting for 
a response.

An indicator is kept on each node to tell whether or not the node represents a page that has successfully been loaded.  
Using HashMaps and HashSets to avoid duplicates, nodes are initially added with the parsed indicator set to false, and
then the indicator is set to true once the page is successfully loaded.  This allows the system to use multi-threading 
to simultaneously follow as many links as the servlet container is capable of, rather than synchronously loading one 
link at a time.  When information is sent to the browser, only valid links are included.  This means that if 
"example.com" contains a link to "example.com/puppies" that doesn't exist, there won't be an "example.com/puppies" 
node in the graphs that are displayed.

### In Memory vs Persistent
There are two options on the page - "crawl (in memory)", and "crawl (persistent)".  Although most sites can be crawled using the "in memory" approach, some very large websites have too many pages to store and will cause the JVM to run out of heap space.  The "persistent" version uses MapDB file-based databases to store most of the information in the graph, which make it the safer of the two options for large sites, although it's several times slower as a result. Since MapDB contains implementations of Java's HashMap and HashSet, the data structure used for this implementation is identical to the in-memory data structure, and shares most of the same source code.

### Future improvements
* Front end - functionality is pretty minimal at this point.  The page styles could use some work, there's no validation of user input yet, and there may be a better graph library to use for displaying very large graphs.
* It would be helpful to add an option to allow the user to specify a maximum depth - i.e. for depth 2, "example.com/users" would be valid but "example.com/users/jared" would be ignored.
