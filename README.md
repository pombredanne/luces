Luces
============

A library to convert Lucene documents to JSON

License: Apache 2.0

Dependencies:
------------
* Java 7
* gson 2.3.1
* lucene-core 3.6.1
* junit 3.8.1

Usage:
------------
For now, the library can be used like so:

```java
Convert.documentToJSON(Document doc);
```
Where doc is a populated Lucene document. A string will beb returned that represents the JSON blob that can be sent to  Elasticsearch.
For testing purposes, you can also pass a boolean to pretty print the output:

```java
Convert.documentToJSON(Document doc, true);
```

TODO:
------------
* Add to central Maven repo: http://maven.apache.org/guides/mini/guide-central-repository-upload.html
* Add more testing
* make code better
