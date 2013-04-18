// $Id: EUtils.java,v 1.23 2013/02/04 19:42:45 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Benjamin Gross, Chris Sander
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
package org.mskcc.cbio.biogene.eutils;

// imports
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;
import java.net.URLDecoder;

import org.mskcc.cbio.biogene.schema.*;
import org.mskcc.cbio.biogene.util.cache.EhCache;

/**
 * Coordinates ESearch and EFetch serivces.
 *
 * @author Benjamin Gross
 */
public class EUtils {

	// some statics
	public static final String ALL_ORGANISMS = "all organisms";

	private static RETURN_CODES code;
    private static Logger log = Logger.getLogger(EUtils.class);
	
	public static enum RETURN_CODES {

		// data types
		SUCCESS("SUCCESS"),
		FAILURE("FAILURE"),
		ID_NOT_FOUND("ID_NOT_FOUND");

		// string ref for readable name
		private String type;

		// constructor
		RETURN_CODES(String type) { this.type = type; }
		
		// method to get enum readable name
		public String toString() { return type; }
	}

    /**
     * Fetches entrez gene info given a symbol and organism.
	 *
	 * Set response to null if you do not want xml to be written.
     *
	 * @param query String
	 * @param organism String
	 * @param retStart Integer
	 * @param retMax Integer
	 * @param outputType String
	 * @param response HttpServletResponse
     */
    public static void processRequest(String query, String organism, Integer retStart, Integer retMax, String outputType, HttpServletResponse response) {

		// decode args
		try {
			query = URLDecoder.decode(query, "UTF-8");
			organism = URLDecoder.decode(organism, "UTF-8");
		}
		catch(Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("EUtils.processRequest(), exception thrown while decoding query or organism: " + e.getMessage());
			}
			if (response != null) {
				Writer.writeError(response, RETURN_CODES.FAILURE, outputType);
			}
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("EUtils.processRequest(), query: " + query);
			log.debug("EUtils.processRequest(), organism: " + organism);
			log.debug("EUtils.processRequest(), retstart: " + retStart);
			log.debug("EUtils.processRequest(), retmax: " + retMax);
		}

		// look in cache for list of ids
		ArrayList<String> IDs = EUtils.getIDs(query, organism);
		if (IDs == null) {
			if (log.isDebugEnabled()) {
				log.debug("EUtils.processRequest(), error executing ESearch.search(), return code: " + EUtils.code.toString());
			}
			if (response != null) {
				Writer.writeError(response, code, outputType);
			}
			return;
		}
		else {
			// we have IDs to work with
			EUtils.processIDs(response, retStart, retMax, IDs, organism, outputType);
		}
    }

	/**
	 * Given a query, determines if it is an advanced one.
	 *
	 * @param query String
	 * @return boolean
	 */
	public static boolean advancedQuery(String query) {
		return (query.contains("[") ||
				query.contains("]") ||
				query.contains("*") ||
				query.contains(" and ") ||
				query.contains(" AND ") ||
				query.contains(" or ") ||
				query.contains(" OR ") ||
				query.contains(" not ") ||
				query.contains(" NOT "));
	}

	/**
	 * Given a query and organism, gets back list of id's.  Null is returned if ids not found.
	 * We all retrieve all ids.
	 * EUtils.code is set by this routine.
	 *
	 * @param query String
	 * @param org String
	 * @return ArrayList<String>
	 */
	private static ArrayList<String> getIDs(String query, String org) {
		
		// look in cache for list of ids
        // (if advanced query, don't use organism in cache key)
		String organism = (EUtils.advancedQuery(query)) ? "" : org; 
		ArrayList<String> IDs = EhCache.checkIDCache(query, organism);

		// IDs not in cache
		if (IDs == null) {
			boolean multitermQuery = (query.contains(" "));
			IDs = new ArrayList<String>();
			// iterate over all search modes, stopping when we have ids
			for (ESearch.SEARCH_MODE mode : ESearch.SEARCH_MODE.values()) {
				// if we have a single-term query, skip search on full gene name
				if (!multitermQuery && (mode == ESearch.SEARCH_MODE.FULL_NAME ||
						                mode == ESearch.SEARCH_MODE.FREE_TEXT_OR)) {
					continue;
				}
				// if we have a multi-term query, skip wildcard searching on [pref] and [sym]
				if (multitermQuery && (mode == ESearch.SEARCH_MODE.PREF ||
						               mode == ESearch.SEARCH_MODE.SYMBOL ||
						               mode == ESearch.SEARCH_MODE.SYMBOL_WILDCARD_RIGHT || 
									   mode == ESearch.SEARCH_MODE.SYMBOL_WILDCARD ||
									   mode == ESearch.SEARCH_MODE.FREE_TEXT_WILDCARD_RIGHT ||
									   mode == ESearch.SEARCH_MODE.FREE_TEXT_WILDCARD)) {
					continue;
				}
				//EUtils.code = ESearch.search(mode, query, organism, 0, Integer.MAX_VALUE, IDs, null);
				// as of Oct 1, 2010, eutils search no longer likes max int as retMAX value - 1M should suffice
				EUtils.code = ESearch.search(mode, query, organism, 0, 1048576, IDs, null);
				if (EUtils.code == RETURN_CODES.SUCCESS) {
					// we successfully found ids, store in cache (unless organism is "All Organisms") and return
					if (!organism.equalsIgnoreCase(EUtils.ALL_ORGANISMS)) {
						EhCache.storeIDInCache(query, organism, IDs);
					}
					return IDs;
				}
			}
		}
        // IDs in cache, outta here
		else {
			return IDs;
		}

		// we have a problem getting ids
		return null;
	}

	/**
	 * Given a set of Id's gets GeneInfo objects and dumps out to response.
	 * Routine accepts retMax, the maximum number of objects to dump.
	 *
	 * @param response HttpServletResponse
	 * @param retStart Integer
	 * @param retMax Integer
	 * @param IDs ArrayList<String>
	 * @param organism String
	 * @param outputType String
	 *
	 */
	private static void processIDs(HttpServletResponse response, Integer retStart, Integer retMax, ArrayList<String> IDs, String organism, String outputType) {

		// setup some required vars
		GeneInfoList geneInfoList = new GeneInfoList();
		geneInfoList.setCount(IDs.size());

		// interate over IDs
		int retEnd = retStart + retMax;
		for (int lc = retStart; lc < retEnd; lc++) {
			
			// get geneID - make sure we don't go out of bounds
			if (lc >= IDs.size()) {
				break;
			}
			String geneID = IDs.get(lc);

			// look in cache for gene info object
			GeneInfo geneInfo = EhCache.checkInfoCache(geneID);
			if (geneInfo != null) {
				// found object in cache, add object to GeneInfoList
				if (log.isDebugEnabled()) {
					log.debug("EUtils.processRequest(), retStart: " + retStart + ", loop counter: " + lc +
							  ", retMax: " + retMax + ", adding geneInfo to return set");
				}
				geneInfoList.getGeneInfo().add(geneInfo);
			}
			// gene info not in cache, fetch
			else {
				geneInfo = EFetch.fetch(geneID);
				if (geneInfo != null) {
					// add to cache for future (unless organism is "All Organisms")
					if (!organism.equalsIgnoreCase(EUtils.ALL_ORGANISMS)) {
						EhCache.storeInfoInCache(geneInfo);
					}
					// add object to return
					if (log.isDebugEnabled()) {
						log.debug("EUtils.processRequest(), retStart: " + retStart + ", loop counter: " + lc +
								  ", retMax: " + retMax + ", adding geneInfo to return set");
					}
					geneInfoList.getGeneInfo().add(geneInfo);
				}
			}
		}

		// made it here
		EUtils.dumpGeneInfoList(response, geneInfoList, outputType);
	}


	/**
	 * Given a GeneInfoList, writes content out to response
	 *
	 * @param response HttpServletResponse
	 * @param geneInfoList GeneInfoList
	 * @param outputType String
	 */
	private static void dumpGeneInfoList(HttpServletResponse response, GeneInfoList geneInfoList, String outputType) {

		// return code is SUCCESS if we have at least one GeneInfo in list - which is most cases
		int geneSize = geneInfoList.getGeneInfo().size();
		String returnCode = (geneSize > 0) ?
			EUtils.RETURN_CODES.SUCCESS.toString() : EUtils.RETURN_CODES.FAILURE.toString();
		geneInfoList.setReturnCode(returnCode);
		geneInfoList.setRetMax(geneSize);

		// if we have a writer, dump list
		if (response != null) {
			Writer.write(response, geneInfoList, outputType);
		}
	}
}
