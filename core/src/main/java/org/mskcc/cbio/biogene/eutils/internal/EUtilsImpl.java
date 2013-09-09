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
package org.mskcc.cbio.biogene.eutils.internal;

import org.mskcc.cbio.biogene.model.*;
import org.mskcc.cbio.biogene.schema.*;
import org.mskcc.cbio.biogene.config.Config;
import org.mskcc.cbio.biogene.eutils.EUtils;

import gov.nih.nlm.ncbi.*;
import gov.nih.nlm.ncbi.www.soap.eutils.esearch.*;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch_gene.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.net.URLEncoder;

class EUtilsImpl implements EUtils {

	private static final String MIM_DB = "MIM";
	private static final String LIST_DELIMITER = ":";
	private static final String SYMBOL_TAG = "SYMBOL";
	private static final String FREETEXT_TAG = "FREETEXT";
	private static final String ORGANISM_TAG = "ORGANISM";

	private EUtilsServiceStub eUtilsService;
	private EFetchGeneServiceStub eFetchGeneService;
	private List<SearchTermMetadata> searchTermMetadatas;

	public EUtilsImpl(Config config) throws Exception
	{
		this.eUtilsService = new EUtilsServiceStub();
		this.eFetchGeneService = new EFetchGeneServiceStub();
		this.searchTermMetadatas = config.getSearchTermMetadata();
	}

	@Override
	public List<String> getGeneIds(OrganismMetadata organismMetadata) throws Exception
	{
		ArrayList<String> geneIds = new ArrayList<String>();

		SearchTermMetadata searchTermMetadata = SearchTermMetadata.getSearchTermMetadata(SearchTermMetadata.SEARCH_MODE.ALL_GENE_IDS,
																						 searchTermMetadatas);
		String encodedSearchTerm = getEncodedSearchTerm(searchTermMetadata.getSearchTerm(), organismMetadata);

		Integer retStart = 0;
		Integer totalNumberFetched = 0;
		Integer totalNumberOfIds = null;
		do {
			ESearchResultDocument.ESearchResult res = getESearchResult(encodedSearchTerm,
																	   searchTermMetadata.getSearchDb(),
																	   retStart.toString(), searchTermMetadata.getRetMax(),
																	   searchTermMetadata.getUseHistory());

			// note - res.getCount() returns total number of gene ids available for organism
			if (totalNumberOfIds == null) {
				totalNumberOfIds = Integer.valueOf(res.getCount());
			}

			if (res.getIdList().getIdArray().length == 0) break;

			List<String> ids = Arrays.asList(res.getIdList().getIdArray());
			geneIds.addAll(ids);
			totalNumberFetched += res.getIdList().getIdArray().length;
			retStart = totalNumberFetched;
		}
		while (totalNumberFetched < totalNumberOfIds);
																   
		return geneIds;
	}

	@Override
	public List<String> getGeneIds(String query, OrganismMetadata organismMetadata) throws Exception
	{
		boolean multiTermQuery = SearchTermMetadata.isMultiTermQuery(query);
		for (SearchTermMetadata.SEARCH_MODE searchMode : SearchTermMetadata.SEARCH_MODE.GENE_ID_FREE_TEXT_SEARCH) {
			if (skipSearchMode(searchMode, multiTermQuery)) continue;
			SearchTermMetadata searchTermMetadata = SearchTermMetadata.getSearchTermMetadata(searchMode,
																							 searchTermMetadatas);
			String encodedSearchTerm = getEncodedSearchTerm(searchMode, query,
															searchTermMetadata.getSearchTerm(), organismMetadata);
			ESearchResultDocument.ESearchResult res = getESearchResult(encodedSearchTerm,
																	   searchTermMetadata.getSearchDb(),
																	   "0", searchTermMetadata.getRetMax(),
																	   searchTermMetadata.getUseHistory());
			if (res.getIdList().getIdArray().length == 0) continue;
			return Arrays.asList(res.getIdList().getIdArray());
		}

		return new ArrayList<String>();
	}

	@Override
	public GeneInfo getGeneInfo(String geneId) throws Exception
	{
		EntrezgeneDocument.Entrezgene[] entrezGenes = getEFetchResult(geneId).getEntrezgeneSet().getEntrezgeneArray();

		if (entrezGenes.length == 1) {
			GeneInfo geneInfo = newGeneInfo(entrezGenes[0]);
			geneInfo.setGeneId(geneId);
			return geneInfo;
		}

		return null;
	}

	private EFetchResultDocument.EFetchResult getEFetchResult(String geneId) throws Exception
	{
		EFetchRequestDocument req = EFetchRequestDocument.Factory.newInstance();
		EFetchRequestDocument.EFetchRequest efr = EFetchRequestDocument.EFetchRequest.Factory.newInstance();
		efr.setId(geneId);
		req.setEFetchRequest(efr);
		return eFetchGeneService.run_eFetch(req).getEFetchResult();
	}

	/**
	 * @param retStart Per NCBI docs - sequential index of the first UID in the retreived set to be shown in the output
	 * @param retMax Per NCBI docs - total number of UIDs from the retreived set to be shown in the output
	 * @param useHistory Per NCBI docs - when set to 'y', ESearch will post the UIDs resulting from search onto
	 *                                   History server to be used directly in subsequent E-utility calls.
	 */
	private ESearchResultDocument.ESearchResult getESearchResult(String searchTerm, String searchDb, String retStart, String retMax, String useHistory) throws Exception
	{
		ESearchRequestDocument req = ESearchRequestDocument.Factory.newInstance();
		ESearchRequestDocument.ESearchRequest esr = ESearchRequestDocument.ESearchRequest.Factory.newInstance();
		esr.setDb(searchDb);
		esr.setTerm(searchTerm);
		esr.setRetStart(retStart);
		esr.setRetMax(retMax);
		esr.setUsehistory(useHistory);
		req.setESearchRequest(esr);
		return eUtilsService.run_eSearch(req).getESearchResult();
	}

	private String getEncodedSearchTerm(SearchTermMetadata.SEARCH_MODE searchMode, String query,
										String searchTerm, OrganismMetadata organismMetadata) throws Exception
	{
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		if (SearchTermMetadata.isAdvancedQuery(encodedQuery)) {
			return getEncodedAdvancedSearchTerm(encodedQuery, organismMetadata);
		}

		searchTerm = getEncodedSearchTerm(searchTerm, organismMetadata);
		
		switch (searchMode) {
		case PREF:
		case SYMBOL:
		case FULL_NAME:
		case SYMBOL_WILDCARD_RIGHT:
		case SYMBOL_WILDCARD:
			return searchTerm.replace(SYMBOL_TAG, encodedQuery);
		case FREE_TEXT:
		case FREE_TEXT_WILDCARD_RIGHT:
		case FREE_TEXT_WILDCARD:
			return searchTerm.replace(FREETEXT_TAG, encodedQuery);
		case FREE_TEXT_OR:
			String orQuery = "(";
			String parts[] = query.split(" ");
			int lc = 0;
			for (String part : parts) {
				orQuery += (++lc < parts.length) ? (part + "[All Fields] OR ") : (part + "[All Fields])");
			}
			String encodedOrQuery = URLEncoder.encode(orQuery, "UTF-8");
			return searchTerm.replace(FREETEXT_TAG, encodedOrQuery);
		default:
			return "";
		}
	}

	private String getEncodedSearchTerm(String searchTerm, OrganismMetadata organismMetadata) throws Exception
	{
		String organism = URLEncoder.encode(organismMetadata.getName(), "UTF-8");
		searchTerm = searchTerm.replace(ORGANISM_TAG, organism);
		return searchTerm;
	}
	
	private String getEncodedAdvancedSearchTerm(String encodedQuery, OrganismMetadata organismMetadata) throws Exception
	{
		String searchTerm = SearchTermMetadata.getSearchTermMetadata(SearchTermMetadata.SEARCH_MODE.ADVANCED_SEARCH,
																	 searchTermMetadatas).getSearchTerm();
		searchTerm = searchTerm.replace(FREETEXT_TAG, encodedQuery);
		return getEncodedSearchTerm(searchTerm, organismMetadata);
	}

	private boolean skipSearchMode(SearchTermMetadata.SEARCH_MODE searchMode, boolean multiTermQuery)
	{
		// if we have a single-term query, skip search on full gene name
		if (!multiTermQuery && (searchMode == SearchTermMetadata.SEARCH_MODE.FULL_NAME ||
								searchMode == SearchTermMetadata.SEARCH_MODE.FREE_TEXT_OR)) {
			return true;
		}
		// if we have a multi-term query, skip wildcard searching on [pref] and [sym]
		else if (multiTermQuery && (searchMode == SearchTermMetadata.SEARCH_MODE.PREF ||
									searchMode == SearchTermMetadata.SEARCH_MODE.SYMBOL ||
									searchMode == SearchTermMetadata.SEARCH_MODE.SYMBOL_WILDCARD_RIGHT || 
									searchMode == SearchTermMetadata.SEARCH_MODE.SYMBOL_WILDCARD ||
									searchMode == SearchTermMetadata.SEARCH_MODE.FREE_TEXT_WILDCARD_RIGHT ||
									searchMode == SearchTermMetadata.SEARCH_MODE.FREE_TEXT_WILDCARD)) {
			return true;
		}
		else {
			return false;
		}
	}

	private GeneInfo newGeneInfo(EntrezgeneDocument.Entrezgene entrezGene)
	{
		GeneInfo geneInfo = new GeneInfo();

        geneInfo.setGeneSummary(entrezGene.getEntrezgeneSummary());
		geneInfo.setGeneOrganism(getGeneOrganism(entrezGene));
		geneInfo.setGeneChromosome(getGeneChromosome(entrezGene));
		geneInfo.setGeneSymbol(getGeneSymbol(entrezGene));
		geneInfo.setGeneTag(getGeneTag(entrezGene));
		geneInfo.setGeneLocation(getGeneLocation(entrezGene));
		geneInfo.setGeneDescription(getGeneDescription(entrezGene));
		geneInfo.setGeneAliases(getGeneAliases(entrezGene));
		geneInfo.setGeneMim(getGeneMimRef(entrezGene));
		geneInfo.setGeneDesignations(getGeneDesignations(entrezGene));
		geneInfo.getGeneRif().addAll(getGeneRIFs(entrezGene));

		return geneInfo;
	}

	private String getGeneOrganism(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return entrezGene.getEntrezgeneSource().getBioSource().getBioSourceOrg().getOrgRef().getOrgRefTaxname();
		}
		catch(NullPointerException e) {
			return null;
		}
	}

	private String getGeneChromosome(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			gov.nih.nlm.ncbi.www.soap.eutils.efetch_gene.SubSourceDocument.SubSource[] subSources = 
				entrezGene.getEntrezgeneSource().getBioSource().getBioSourceSubtype().getSubSourceArray();
			return (subSources.length > 0) ? subSources[0].getSubSourceName() : null;
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneSymbol(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefLocus();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneTag(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefLocusTag();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneLocation(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefMaploc();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneDescription(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefDesc();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneAliases(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return getArrayAsString(entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefSyn().getGeneRefSynEArray());
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneMimRef(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return getRefDb(MIM_DB, entrezGene);
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private String getGeneDesignations(EntrezgeneDocument.Entrezgene entrezGene)
	{
		try {
			return getArrayAsString(entrezGene.getEntrezgeneProt().getProtRef().getProtRefName().getProtRefNameEArray());
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	private List<GeneRIF> getGeneRIFs(EntrezgeneDocument.Entrezgene entrezGene)
	{
		List<GeneRIF> rifs = new ArrayList<GeneRIF>();
		if (entrezGene.isSetEntrezgeneComments()) {
			for (GeneCommentaryDocument.GeneCommentary commentary : entrezGene.getEntrezgeneComments().getGeneCommentaryArray()) {
                if (commentary.getGeneCommentaryType().getValue() == GeneCommentaryDocument.GeneCommentary.GeneCommentaryType.Value.GENERIF) {
					GeneRIF rif = new GeneRIF();
                    rif.setRif(commentary.getGeneCommentaryText());
					if (commentary.getGeneCommentaryRefs() != null) {
						PubDocument.Pub[] pubs = commentary.getGeneCommentaryRefs().getPubArray();
						if (pubs.length > 0) {
							rif.setPubmedId(pubs[0].getPubPmid().getPubMedId().intValue());
							rifs.add(rif);
						}
					}
                }
			}
		}
		return rifs;
	}

	private String getRefDb(String refDb, EntrezgeneDocument.Entrezgene entrezGene)
	{
		for (DbtagDocument.Dbtag tag : entrezGene.getEntrezgeneGene().getGeneRef().getGeneRefDb().getDbtagArray()) {
            if (tag.getDbtagDb().equals(refDb)) {
			    return tag.getDbtagTag().getObjectId().getObjectIdId().toString();
            }
		}
		return null;
	}

	private String getArrayAsString(String[] list)
	{
		String items = "";
		for (String item : list) {
			items += item + LIST_DELIMITER;
		}
		return (items.isEmpty()) ? null : items.substring(0, items.length()-1);
	}
}
