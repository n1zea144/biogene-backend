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
package org.mskcc.cbio.biogene.importer.internal;

import org.mskcc.cbio.biogene.schema.*;
import org.mskcc.cbio.biogene.cache.CacheManager;

import org.apache.commons.logging.*;

import org.springframework.stereotype.Component;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Component("uniProtMappingWriter")
public class UniProtMappingWriterImpl implements ItemWriter<UniProtMapping>
{
	private static final Log LOG = LogFactory.getLog(UniProtMappingWriterImpl.class);

	@Autowired
	@Qualifier("biogeneCacheManager")
	private CacheManager cacheManager;

	@Override
	public void write(List<? extends UniProtMapping> items) throws Exception
	{
		for (UniProtMapping mapping : items) {
			GeneInfo geneInfo = cacheManager.getGeneInfo(mapping.getId());
			if (geneInfo == null) continue;
			if (geneInfoNeedsUpdate(geneInfo, mapping)) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Updating UniProt AC list for " + geneInfo.getGeneId() + ", adding: " + mapping.getUniProtAC());
				}
				updateUniProtAC(geneInfo, mapping);
			}
		}
	}

	private boolean geneInfoNeedsUpdate(GeneInfo geneInfo, UniProtMapping mapping)
	{
		return (geneInfo.getGeneUniprotMapping() == null ||
				!geneInfo.getGeneUniprotMapping().contains(mapping.getUniProtAC()));
	}

	private void updateUniProtAC(GeneInfo geneInfo, UniProtMapping mapping)
	{
		String uniProtACs = geneInfo.getGeneUniprotMapping();
		if (uniProtACs == null || uniProtACs.isEmpty()) {
			uniProtACs = mapping.getUniProtAC();
		}
		else {
			uniProtACs += UniProtMapping.UNIPROT_AC_DELIMITER + mapping.getUniProtAC();
		}
		geneInfo.setGeneUniprotMapping(uniProtACs);
		cacheManager.updateGeneInfo(geneInfo.getGeneId(), geneInfo);
	}
}
