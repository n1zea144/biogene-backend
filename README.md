# biogene-backend
This biogene-backend project provides web-service support for the [biogene-ios-client](https://github.com/n1zea144/biogene-ios-client).  It provides entrez-gene information to a client based on a web request which supplies an organism and hugo symbol.  It does this by querying the entrez web service and building a cache (EhCache) of gene information on startup or as a background process, so requests to entrez are not made in realtime.

## Configuration
Prior to compilation, the following configuration files to be created with content properly set:

### biogene.properties

biogene.properties should be copied from [biogene.properties.example](https://github.com/n1zea144/biogene-backend/blob/master/web/src/main/resources/biogene.properties.example) and placed in [biogene-backend/web/src/main/resources](https://github.com/n1zea144/biogene-backend/tree/master/web/src/main/resources).
The following properties need to be set:

* cache.path (root directory where the biogene-backend ehCache repository should be built, see EhCache below)
* uniprot.mapping.path (location where entrez to uniprot id mapping file can be found, see uniprot-cache below)
* global.vars.abstract (used by sencha web interface), replace URL_TO_WEBSERVICE with proper url
* global.vars.search (used by sencha web interface), replace URL_TO_WEBSERVICE with proper url

### log4j.properties
log4j.properties should be copied from [log4j.properties.example](https://github.com/n1zea144/biogene-backend/blob/master/web/src/main/resources/log4j.properties.example) and placed in [biogene-backend/web/src/main/resources](https://github.com/n1zea144/biogene-backend/tree/master/web/src/main/resources).
The following properties do not include a path, only a filename.  They should be updated to contain the proper path to the logfile:

* log4j.appender.a.rollingPolicy.FileNamePattern
* log4j.appender.a.File

### Compilation

biogene-backend should be compiled with a Java version prior to Java 8.  To compile the code, run the following command in the root directory:

```
mvn clean install
```

After running this command you should find the following web archive: ***web/target/biogene.war***.  After this war file is deployed, you are ready to populate the EhCache.

### EhCache

The biogene webservice provides gene information to clients by using the entrez web service.  To avoid the overhead of querying the entrez service in realtime, it builds a cache of all required gene information and uses this cache to satisfy requests to the biogene web service.  There are three caches that the biogene webservice maintains, the root location of which is specified by biogene.properties:cache.path:

* uniprot-cache
* gene-ids-cache
* gene-info-cache

#### uniprot-cache

The uniprot cache is used to associate entrez gene ids with corresponding protein names found within the uniprot system.  This association is used to annotate entries in gene-info-cache.  When the biogene-ios-client receives gene info from the biogene web service, it is able to create links to the uniprot system.  The uniprot cache is built from a manually curated tsv containing one column for entrez_ids and one column for uniprot protein names.  Here is an example snippet for entrez id 7157 (TP53):

```
7157	H2EHT1
7157	K7PPA8
7157	P04637
```

As previously mentioned, the path to this file is specified in biogene.properties:uniprot.mapping.path.  After war deployment, this cache can be populated by making the following request (replace URL_TO_WEBSERVICE with proper url):

```
URL_TO_WEBSERVICE/retrieve.do?cmd=u
```

***Note, its important to populate this cache before gene-ids-cache and gene-info-cache.***

#### gene-ids-cache, gene-info-cache

The gene-ids-cache is used to associated hugo gene symbols with entrez ids.  The gene-info-cache is used to associated entrez ids with gene information (summary, gene-rifs).  Since the biogene-ios-client supports a predefined set of organisms, to avoid cache misses its important to load data for the following organisms: human, mouse, rat, zebrafish, fruit fly, yeast, nematode, arabidopsis.  After war deployment, these caches can be populated by making the following requests (replace URL_TO_WEBSERVICE with proper url):

```
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=human&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=mouse&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=rat&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=zebrafish&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=fruit%20fly&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=yeast&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=nematode&fetch=50&total=-1&delay=5000
URL_TO_WEBSERVICE/retrieve.do?cmd=p&retstart=0&org=arabidopsis&fetch=50&total=-1&delay=5000
```

To check if the webservice has been deployed and the caches properly populated, you can make the following request for gene information:

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


