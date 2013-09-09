/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.biogene.model;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class SearchTermMetadata
{
	public static enum SEARCH_MODE {
		ALL_GENE_IDS("ALL_GENE_IDS"),
		ADVANCED_SEARCH("ADVANCED_SEARCH"),
		PREF("PREF"),
		SYMBOL("SYMBOL"),
		FULL_NAME("FULL_NAME"),
		SYMBOL_WILDCARD_RIGHT("SYMBOL_WILDCARD_RIGHT"),
		SYMBOL_WILDCARD("SYMBOL_WILDCARD"),
		FREE_TEXT("FREE_TEXT"),
		FREE_TEXT_WILDCARD_RIGHT("FREE_TEXT_WILDCARD_RIGHT"),
		FREE_TEXT_WILDCARD("FREE_TEXT_WILDCARD"),
		FREE_TEXT_OR("FREE_TEXT_OR");

		// string ref for readable name
		private String searchMode;

		// constructor
		SEARCH_MODE(String searchMode) { this.searchMode = searchMode; }

		private static final SEARCH_MODE[] GENE_ID_FREE_TEXT_SEARCH_ORDER = { PREF, SYMBOL, FULL_NAME,
																			  SYMBOL_WILDCARD_RIGHT, SYMBOL_WILDCARD,
																			  FREE_TEXT, FREE_TEXT_WILDCARD_RIGHT,
																			  FREE_TEXT_WILDCARD, FREE_TEXT_OR };
		public static final List<SEARCH_MODE> GENE_ID_FREE_TEXT_SEARCH = Collections.unmodifiableList(Arrays.asList(GENE_ID_FREE_TEXT_SEARCH_ORDER));

		// method to get enum readable name
		public String toString() { return searchMode; }
	}

	// bean properties
	private String mode;
	private String retMax;
	private String searchDb;
	private String searchTerm;
	private String useHistory;

    public SearchTermMetadata(String[] properties)
	{
		if (properties.length < 5) {
            throw new IllegalArgumentException("corrupt properties array passed to constructor");
		}

		this.mode = properties[0].trim();
		this.searchTerm = properties[1].trim();
		this.searchDb = properties[2].trim();
		this.retMax = properties[3].trim();
		this.useHistory = properties[4].trim();
	}

	public static SearchTermMetadata getSearchTermMetadata(SEARCH_MODE mode, List<SearchTermMetadata> searchTermMetadatas)
	{
		for (SearchTermMetadata searchTermMetadata : searchTermMetadatas) {
			if (searchTermMetadata.getMode().equals(mode)) {
				return searchTermMetadata;
			}
		}
		return null;
	}

	public static boolean isMultiTermQuery(String query)
	{
		return (query.contains(" "));
	}

	public static boolean isAdvancedQuery(String query)
	{
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

	public String getMode() { return mode; }
	public String getRetMax() { return retMax; }
	public String getSearchDb() { return searchDb; }
	public String getSearchTerm() { return searchTerm; }
	public String getUseHistory() { return useHistory; }
}
