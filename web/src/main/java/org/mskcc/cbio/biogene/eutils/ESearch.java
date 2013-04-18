// $Id: ESearch.java,v 1.21 2012/02/24 20:12:27 grossb Exp $
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

import org.mskcc.cbio.biogene.schema.*;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Searches for entrez gene info from ncbi.  Used to 
 * convert gene symbol to gene id.
 *
 * @author Benjamin Gross
 */
public class ESearch {

	// some statics
	public static enum SEARCH_MODE {

		// modes
		PREF("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+SYMBOL[pref]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		SYMBOL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+(SYMBOL*[pref]+OR+SYMBOL[sym])+AND+ORGANISM[orgn]&retstart=RETSTART"),
		FULL_NAME("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+SYMBOL[gene+full+name]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		SYMBOL_WILDCARD_RIGHT("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+SYMBOL*[sym]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		SYMBOL_WILDCARD("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+*SYMBOL*[sym]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		FREE_TEXT("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+FREETEXT[All+Fields]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		FREE_TEXT_WILDCARD_RIGHT("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+FREETEXT*[All+Fields]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		FREE_TEXT_WILDCARD("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+*FREETEXT*[All+Fields]+AND+ORGANISM[orgn]&retstart=RETSTART"),
		FREE_TEXT_OR("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+AND+FREETEXT+AND+ORGANISM[orgn]&retstart=RETSTART");

		// string ref for readable name
		private String searchMode;

		// constructor
		SEARCH_MODE(String searchMode) { this.searchMode = searchMode; }

		// method to get enum readable name
		public String toString() { return searchMode; }
	}

	// some statics
    private static Logger log = Logger.getLogger(ESearch.class);
	private static String ADVANCED_SEARCH_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+FREETEXT&retstart=RETSTART";
	private static String CACHE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=alive[prop]+NOT+newentry[title]+ORGANISM[orgn]&retstart=RETSTART";

    /**
     * Converts gene symbol & organism combination to one or more gene ids.
	 *
	 * If searchSymbol is true, symbol search is performed, else free text search.
     *
	 * @param searchSymbol boolean
     * @param symbol String
     * @param organism String
	 * @param retStart int
	 * @param retMax int
	 * @param IDs ArrayList<String>
	 * @param totalCount Integer[]
	 * @return String
     */
    public static EUtils.RETURN_CODES search(SEARCH_MODE searchMode, String query, String organism, int retStart, int retMax, ArrayList<String> IDs, Integer[] totalCount) {

		try {
			// fetch from entrez gene
			// node query is encoded within getSearchModeURL
			String url = ESearch.getSearchModeURL(searchMode, query);
			url = url.replace("RETSTART", Integer.toString(retStart));
			//url = url.replace("RETMAX", Integer.toString(retMax));
			if (organism.equalsIgnoreCase(EUtils.ALL_ORGANISMS)) {
				url = url.replace("+AND+ORGANISM[orgn]", "");
			}
			else {
				organism = URLEncoder.encode(organism, "UTF-8");
				url = url.replace("ORGANISM", organism);
			}
			if (log.isDebugEnabled()) {
				log.debug("ESearch.search(), url: " + url);
			}
			return getIDs(url, IDs, totalCount);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception", e);
			}
			return EUtils.RETURN_CODES.FAILURE;
		}
    }

    /**
     * Fetches ids for a given organsms starting at desired start record number and returning max records.
     *
     * @param organism String
	 * @param retStart int
	 * @param retMax int
	 * @param IDs ArrayList<String>
	 * @param totalCount Integer[]
	 * @return String
     */
    public static EUtils.RETURN_CODES search(String organism, int retStart, int retMax, ArrayList<String> IDs, Integer[] totalCount) {

		try {
			String url = CACHE_URL.replace("RETSTART", Integer.toString(retStart));
			//url = url.replace("RETMAX", Integer.toString(retMax));
			if (organism.equalsIgnoreCase(EUtils.ALL_ORGANISMS)) {
				url = url.replace("+ORGANISM[orgn]", "");
			}
			else {
				organism = URLEncoder.encode(organism, "UTF-8");
				url = url.replace("ORGANISM", organism);
			}
			return getIDs(url, IDs, totalCount);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception", e);
			}
			return EUtils.RETURN_CODES.FAILURE;
		}
	}

	/**
	 * Given a url, searches for ids via esearch.
	 *
	 * @param url String
	 * @param IDs ArrayList
	 * @param totalCount Integer[]
	 */
	private static EUtils.RETURN_CODES getIDs(String url, ArrayList<String> IDs, Integer[] totalCount) {

		try {
			String content = Retriever.connect(url);

			// no content, error retrieving
			if (content == null) {
				// problem connecting
				if (log.isDebugEnabled()) {
					log.debug("ESearch.search(), connect error.");
				}
				return EUtils.RETURN_CODES.FAILURE;
			}
			// parse
			ArrayList<String> res = ESearchParser.parse(content, totalCount);
			if (res == null) {
				return EUtils.RETURN_CODES.FAILURE;
			}
			else if (res.size() == 0) {
				if (log.isDebugEnabled()) {
					log.debug("ESearch.search(), id not found.");
				}
				return EUtils.RETURN_CODES.ID_NOT_FOUND;
			}
			if (log.isDebugEnabled()) {
				log.debug("ESearch.search(), gene id(s) retrieval success.");
			}
			IDs.addAll(res);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception", e);
			}
			return EUtils.RETURN_CODES.FAILURE;
		}

		// outta here
		return EUtils.RETURN_CODES.SUCCESS;
	}

	/**
	 * Given a search mode and query string, returns a url as string.
	 *
	 * @param searchMode SEARCH_MODE
	 * @param query String
	 * @return String
	 */
	private static String getSearchModeURL(SEARCH_MODE searchMode, String query) throws Exception {

		String encodedQuery = URLEncoder.encode(query, "UTF-8");

		// check for advanced query
		if (EUtils.advancedQuery(query)) {
			if (log.isDebugEnabled()) {
				log.debug("ESearch.getSearchModeURL() - we have an advanced query...");
			}
			return ADVANCED_SEARCH_URL.replace("FREETEXT", encodedQuery);
		}

		switch (searchMode) {
		    case PREF:
		    case SYMBOL:
		    case FULL_NAME:
		    case SYMBOL_WILDCARD_RIGHT:
		    case SYMBOL_WILDCARD:
			    return searchMode.toString().replace("SYMBOL", encodedQuery);
		    case FREE_TEXT:
		    case FREE_TEXT_WILDCARD_RIGHT:
		    case FREE_TEXT_WILDCARD:
			    return searchMode.toString().replace("FREETEXT", encodedQuery);
		    case FREE_TEXT_OR:
				String orQuery = "(";
				String parts[] = query.split(" ");
				int lc = 0;
				for (String part : parts) {
					orQuery += (++lc < parts.length) ? (part + "[All Fields] OR ") : (part + "[All Fields])");
				}
				String encodedOrQuery = URLEncoder.encode(orQuery, "UTF-8");
				return searchMode.toString().replace("FREETEXT", encodedOrQuery);
		}
		
		// should not get here
		return "";
	}
}
