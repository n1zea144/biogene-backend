# biogene-backend
This biogene-backend project provides web-service support for the [biogene-ios-client](https://github.com/n1zea144/biogene-ios-client).  It provides entrez-gene information to a client based on a web request which supplies an organism and hugo symbol.  It does this by querying the entrez web service and building a cache (EhCache) of gene information on startup or as a background process, so requests to entrez are not made in realtime.

### Compilation

biogene-backend should be compiled with a Java version prior to Java 8.  To compile the code, run the following command in the root directory:

```
mvn clean install
```

After running this command you should find the following web archive: ***web/target/biogene.war***.  After this war file is deployed, you are ready to populate the EhCache.


## System Properities

The following system properties should be past to the JVM:

* BIOGENE_EHCACHE_PATH (path on disk which designates where EhCache will be written)
* BIOGENE_LOGFILE_PATH (path to web application log4j log file)


### EhCache

The biogene webservice provides gene information to clients by using the entrez web service.  To avoid the overhead of querying the entrez service in realtime, it builds a cache of all required gene information and uses this cache to satisfy requests to the biogene web service.  After deployment, the following web request should be performed to populate a required UniProt mapping cache, (replace URL_TO_WEBSERVICE with proper url):

```
URL_TO_WEBSERVICE/retrieve.do?cmd=u
```

***Note, its important to populate this cache before gene-ids-cache and gene-info-cache.***

To check if the webservice has been deployed and the UniProt cache properly populated, you can make the following request for gene information:

```
URL_TO_WEBSERVICE/retrieve.do?query=GENE_SYMBOL&org=ORGANISM
```

For example to query info for the TP53 human homolog:

```
URL_TO_WEBSERVICE/retrieve.do?query=BRCA1&org=HUMAN
```

This will return an XML document for BRCA1 gene information.  One of the very first elements should be a return_code:

```
<return_code>SUCCESS</return_code>
```

and somewhere within the document should be a gene_uniprot_mapping element:

```
<gene_uniprot_mapping>E7ETR2:E9PFC7:E9PFZ0:P38398:Q1RMC1</gene_uniprot_mapping
```

## Deployment
[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)
