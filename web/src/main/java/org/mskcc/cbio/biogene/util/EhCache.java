// $Id: EhCache.java,v 1.9 2012/02/24 20:12:27 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.cbio.biogene.util.cache;

// imports
import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.base.Joiner;

import org.mskcc.cbio.biogene.schema.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Global Cache.
 *
 * @author Ethan Cerami.
 */
public class EhCache {

	// some statics
    private static Logger log = Logger.getLogger(EhCache.class);

    /**
     * Name of In-Memory Cache.
     */
    public static final String BIOGENE_ID_CACHE = "biogene_cache_gene_ids";
    public static final String BIOGENE_INFO_CACHE = "biogene_cache_gene_info";
    public static final String BIOGENE_UNIPROT_MAPPING_CACHE = "biogene_cache_uniprot_mapping";

	private static CacheManager geneIDsCacheManager;
	private static CacheManager geneInfoCacheManager;
	private static CacheManager uniProtMappingCacheManager;

    /**
     * Initializes the EhCache with ehcache.xml.
     *
     * @throws CacheException Error Initializing Cache.
     */
    public static void initCache() throws CacheException {
        //  Create a CacheManager using ehcache.xml
		if (log.isInfoEnabled()) {
			log.info("EhCache, initializing cache");
		}
		shutDownCache();
		geneIDsCacheManager = new CacheManager(EhCache.class.getResourceAsStream("/ehcache-gene-ids.xml"));
		geneInfoCacheManager = new CacheManager(EhCache.class.getResourceAsStream("/ehcache-gene-info.xml"));
		uniProtMappingCacheManager = new CacheManager(EhCache.class.getResourceAsStream("/ehcache-uniprot-mapping.xml"));
    }

    /**
     * Shuts down EhCache.
     */
    public static void shutDownCache() {
		if (geneIDsCacheManager != null) geneIDsCacheManager.shutdown();
		if (geneInfoCacheManager != null) geneInfoCacheManager.shutdown();
		if (uniProtMappingCacheManager != null) uniProtMappingCacheManager.shutdown();
    }

    /**
     * Resets all EhCaches.
     *
     * @throws IOException IO Error.
     */
    public static void resetAllCaches() throws IOException {
		if (log.isInfoEnabled()) {
			log.info("EhCache, resetting all caches");
		}
		geneIDsCacheManager.getCache(BIOGENE_ID_CACHE).removeAll();
		geneInfoCacheManager.getCache(BIOGENE_INFO_CACHE).removeAll();
		uniProtMappingCacheManager.getCache(BIOGENE_UNIPROT_MAPPING_CACHE).removeAll();
    }

	/**
	 * Checks cache.
	 *
	 * @param query String
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> checkIDCache(String query, String organism) {

		String key = (organism.length() > 0) ? (query.toLowerCase() + "-" + organism.toLowerCase()) : query;
		if (log.isInfoEnabled()) {
			log.info("EhCache, checking " + BIOGENE_ID_CACHE + " cache for key: " + key);
		}
		Cache cache = geneIDsCacheManager.getCache(BIOGENE_ID_CACHE);
		Element element = cache.get(key);
		if (element != null) {
			if (log.isInfoEnabled()) {
				log.info("--> Hit!");
			}
			ArrayList<String> toReturn = (ArrayList<String>)element.getValue();
			if (log.isInfoEnabled()) {
				log.info("--> Size: " + toReturn.size());
			}
			for (String id : toReturn) {
				if (log.isInfoEnabled()) {
					log.info("--> ID: " + id);
				}
			}
			return toReturn;
		}
		else {
			return null;
		}
	}

	/**
	 * Given query & organism, stores ArrayList<String> ids in cache.
	 *
	 * @param query String
	 * @param organism String
	 * @param IDs ArrayList<String>
	 */
	public static void storeIDInCache(String query, String organism, ArrayList<String> ids) {

		Cache cache = geneIDsCacheManager.getCache(BIOGENE_ID_CACHE);
		String key = (organism.length() > 0) ? (query.toLowerCase() + "-" + organism.toLowerCase()) : query;
		if (log.isInfoEnabled()) {
			log.info("EhCache, storing object in " + BIOGENE_ID_CACHE + " cache, key: " + key);
		}
		Element element = new Element(key, ids);
		cache.put(element);
	}

	/**
	 * Checks cache.
	 *
	 * @param geneID String
	 * @return GeneInfo
	 */
	public static GeneInfo checkInfoCache(String geneID) {

		if (log.isInfoEnabled()) {
			log.info("EhCache, checking " + BIOGENE_INFO_CACHE + " cache for key: " + geneID);
		}
		Cache cache = geneInfoCacheManager.getCache(BIOGENE_INFO_CACHE);
		Element element = cache.get(geneID);
		if (element != null) {
			if (log.isInfoEnabled()) {
				log.info("--> Hit!");
			}
			GeneInfo geneInfo = (GeneInfo)element.getValue();
			if (geneInfo.getGeneUniprotMapping() == null) {
				if (log.isInfoEnabled()) log.info("--> Attempting to add UniProtMapping...");
				// add uniprot mapping to GeneInfo
				Vector<String> uniProtMapping = checkUniProtMappingCache(geneID);
				if (uniProtMapping != null) geneInfo.setGeneUniprotMapping(Joiner.on(":").join(uniProtMapping));
				// update GeneInfo object in cache
				element = new Element(geneID, geneInfo);
				cache.put(element);
			}
			return geneInfo;
		}
		else {
			return null;
		}
	}

	/**
	 * Stores GeneInfo object in cache.  Uses GeneInfo.getGeneId() as key.
	 *
	 * @param geneInfo GeneInfo
	 */
	public static void storeInfoInCache(GeneInfo geneInfo) {

		Cache cache = geneInfoCacheManager.getCache(BIOGENE_INFO_CACHE);
		if (log.isInfoEnabled()) {
			log.info("EhCache, storing object in " + BIOGENE_INFO_CACHE + " cache, key: " + geneInfo.getGeneId());
		}
		Element element = new Element(geneInfo.getGeneId(), geneInfo);
		cache.put(element);
	}

	/**
	 * Checks UniProt cache.
	 *
	 * @param geneID String
	 * @return Vector<String>
	 */
	public static Vector<String> checkUniProtMappingCache(String geneID) {

		Cache cache = uniProtMappingCacheManager.getCache(BIOGENE_UNIPROT_MAPPING_CACHE);
		Element element = cache.get(geneID);
		return (element != null) ? (Vector<String>)element.getValue() : null;
	}

	/**
	 * Stores UniProt mapping in cache.
	 *
	 * @param geneID String
	 * @param uniProtID String
	 */
	public static void storeUniProtMappingInCache(String geneID, String uniProtID) {

		Cache cache = uniProtMappingCacheManager.getCache(BIOGENE_UNIPROT_MAPPING_CACHE);
		Vector<String> uniProtIDs = (cache.isKeyInCache(geneID)) ?
			uniProtIDs = checkUniProtMappingCache(geneID) : new Vector<String>();
		if (!uniProtIDs.contains(uniProtID)) uniProtIDs.add(uniProtID);
		Element element = new Element(geneID, uniProtIDs);
		cache.put(element);
	}
}
