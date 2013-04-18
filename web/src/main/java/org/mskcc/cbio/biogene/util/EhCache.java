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

import org.mskcc.cbio.biogene.schema.*;

import java.io.IOException;
import java.util.ArrayList;

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
        CacheManager manager = CacheManager.getInstance();
    }

    /**
     * Shuts down EhCache.
     */
    public static void shutDownCache() {
        CacheManager manager = CacheManager.getInstance();
        manager.shutdown();
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
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(BIOGENE_ID_CACHE);
        cache.removeAll();
		cache = manager.getCache(BIOGENE_INFO_CACHE);
        cache.removeAll();
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
		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache(BIOGENE_ID_CACHE);
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

		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache(BIOGENE_ID_CACHE);

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
		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache(BIOGENE_INFO_CACHE);
		Element element = cache.get(geneID);
		if (element != null) {
			if (log.isInfoEnabled()) {
				log.info("--> Hit!");
			}
			return (GeneInfo)element.getValue();
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

		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache(BIOGENE_INFO_CACHE);

		if (log.isInfoEnabled()) {
			log.info("EhCache, storing object in " + BIOGENE_INFO_CACHE + " cache, key: " + geneInfo.getGeneId());
		}
		Element element = new Element(geneInfo.getGeneId(), geneInfo);
		cache.put(element);
	}
}
