Luces
============

A library to convert Lucene documents to JSON for Elasticsearch consumption

License: Apache 2.0

Dependencies:
------------
* Java 7
* gson 2.3.1
* lucene-core 3.6.1
* junit 3.8.1

Usage:
------------
The library can be used like so:

```java
Luces lucesConverter = new Luces(org.apache.lucene.util.Version.LUCENE_36)
JsonElement jsonDoc = lucesConverter.documentToJSON(Document doc);
```
Where doc is a populated Lucene document. A JsonObject will be returned that can be sent to Elasticsearch.
For testing purposes, you can also pass a boolean to pretty print the output:

```java
String jsonBlob = luceneConverter.documentToJSONStringified(Document doc, true);
```

You can also specify an elasticsearch mapping JSON blob, which will enable any string values that are supposed to be integers, floats, etc. into their correct types
For example, a mapping like:
```javascript
{
  "user": {
    "properties": {
      "name": {
        "type": "string"
      },
      "id": {
        "type": "integer"
      }
    }
  }
}
```
can be added to the converter this way, as a JsonObject:

```java
luceneConverter.mapping("user", mappingJsonObject)
```
where "user" is the type in Elasticsearch.

When using a mapping file, empty values will not be parsed correctly, so you can specify if you want empty values replaced with the defaults for the type:
```java
luceneConverter.useDefaultsForEmpty(true);
```

Otherwise, it defaults to false, and will throw a NumberFormatException when a value is empty. Keep in mind that invalid values (like 123abc in an integer field) will still throw parsing errors regardless of the flag

TODO:
------------
* Handle mappings with hierarchical levels to type determination
* Add to central Maven repo: http://maven.apache.org/guides/mini/guide-central-repository-upload.html
