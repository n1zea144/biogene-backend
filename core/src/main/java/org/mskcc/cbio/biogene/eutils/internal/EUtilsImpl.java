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

import org.mskcc.cbio.biogene.config.Config;
import org.mskcc.cbio.biogene.eutils.EUtils;
import org.mskcc.cbio.biogene.model.*;

import gov.nih.nlm.ncbi.*;
import gov.nih.nlm.ncbi.www.soap.eutils.esearch.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.net.URLEncoder;

class EUtilsImpl implements EUtils {

	private static final String ORGANISM_TAG = "<ORGANISM>";
	private static final Log LOG = LogFactory.getLog(EUtilsImpl.class);

	private List<SearchTermMetadata> searchTermMetadatas;
	private EUtilsServiceStub service;

	public EUtilsImpl(Config config) throws Exception
	{
		this.service = new EUtilsServiceStub();
		this.searchTermMetadatas = config.getSearchTermMetadata();

	}

	@Override
	public List<String> getGeneIds(OrganismMetadata organismMetadata) throws Exception
	{
		ArrayList<String> geneIds = new ArrayList<String>();

		SearchTermMetadata searchTermMetadata = searchTermMetadatas.get(SearchTermMetadata.ALL_GENE_IDS_INDEX);
		String encodedSearchTerm = getEncodedSearchTerm(searchTermMetadata.getSearchTerm(), organismMetadata);

		Integer retStart = 0;
		Integer totalNumberFetched = 0;
		Integer totalNumberOfIds = null;
		do {
			ESearchResultDocument.ESearchResult res = getESearchResult(encodedSearchTerm,
																	   searchTermMetadata.getSearchDb(),
																	   retStart.toString(), searchTermMetadata.getRetMax());

			// note - res.getCount() returns total number of gene ids available for organism
			if (totalNumberOfIds == null) totalNumberOfIds = Integer.valueOf(res.getCount());

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
	public void getGeneInfo(String geneId) throws Exception
	{
		System.out.println("gene info for: " + geneId);
	}

	private ESearchResultDocument.ESearchResult getESearchResult(String searchTerm, String searchDb, String retStart, String retMax) throws Exception
	{
		ESearchRequestDocument req = ESearchRequestDocument.Factory.newInstance();
		ESearchRequestDocument.ESearchRequest esr = ESearchRequestDocument.ESearchRequest.Factory.newInstance();
		esr.setDb(searchDb);
		esr.setTerm(searchTerm);
		esr.setRetStart(retStart);
		esr.setRetMax(retMax);
		req.setESearchRequest(esr);
		return service.run_eSearch(req).getESearchResult();
	}

	private String getEncodedSearchTerm(String searchTerm, OrganismMetadata organismMetadata) throws Exception
	{
		String organism = URLEncoder.encode(organismMetadata.getName(), "UTF-8");
		searchTerm = searchTerm.replace(ORGANISM_TAG, organism);
		return searchTerm;
	}
}
