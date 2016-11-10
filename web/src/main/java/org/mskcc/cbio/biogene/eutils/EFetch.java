// $Id: EFetch.java,v 1.4 2012/02/24 20:12:27 grossb Exp $
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

import java.util.List;

/**
 * Fetches entrez gene info from ncbi.
 *
 * @author Benjamin Gross
 */
public class EFetch {

	// some statics
    private static Logger log = Logger.getLogger(EFetch.class);
	private static String URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&retmode=xml&id=";

	/**
	 * Given an id, fetches gene info
	 *
	 * @param geneID String
	 * @return GeneInfo
	 */
	public static GeneInfo fetch(String geneID) {
		try {
			// fetch from entrez gene
			String url = URL + geneID;
			String content = Retriever.connect(url);

			// if we have no content, skip
			if (content == null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetch.fetch(), no content, id: " + geneID);
				}
				return null;
			}
			
			// content is not null, parse
			GeneInfo geneInfo = EFetchParser.parse(content);
			if (geneInfo == null) {
				if (log.isDebugEnabled()) {
					log.debug("EFetch.fetch(), parse error, id: " + geneID);
				}
				return null;
			}

			// made it here
			if (log.isDebugEnabled()) {
				log.debug("EFetch.fetch(), geneInfo retrieval success, id: " + geneID);
			}
			geneInfo.setGeneId(geneID);

			// outta here
			return geneInfo;
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception", e);
			}
			return null;
		}
	}

    /**
     * Given a list of ids, fetches gene info.
	 *
	 * This routine assumes IDs size > 0.  For each id, a GeneInfo object is constructed.  If 
	 * an exception is encountered, the routine continues to the next id to process.
	 * The return code of the GeneInfoList object returned is set to EUtils.RETURN_CODES.SUCCESS
	 * if contains at least one GeneInfo object.
     *
     * @param IDs List<String>
	 * @return GeneInfoList
     */
    public static GeneInfoList fetch(List<String> IDs) {

		// what we will marshall
		GeneInfoList toReturn = new GeneInfoList();

		// interate over all ids
		for (String geneID : IDs) {
			GeneInfo geneInfo = EFetch.fetch(geneID);
			if (geneInfo != null) {
				toReturn.getGeneInfo().add(geneInfo);
			}
		}

		// return code is SUCCESS if we have at least one GeneInfo in list - which is most cases
		int geneSize = toReturn.getGeneInfo().size();
		String returnCode = (geneSize > 0) ?
			EUtils.RETURN_CODES.SUCCESS.toString() : EUtils.RETURN_CODES.FAILURE.toString();
		toReturn.setReturnCode(returnCode);
		toReturn.setCount(geneSize);

		// outta here
		return toReturn;
    }
}
