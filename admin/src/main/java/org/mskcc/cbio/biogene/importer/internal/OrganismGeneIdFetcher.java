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

import org.mskcc.cbio.biogene.eutils.EUtils;
import org.mskcc.cbio.biogene.model.OrganismMetadata;

import org.apache.commons.logging.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.LinkedBlockingQueue;

public class OrganismGeneIdFetcher
{
	private static final Log LOG = LogFactory.getLog(OrganismMetadataFetcher.class);

	@Autowired
	@Qualifier("organismMetadataQueue")
	private LinkedBlockingQueue<OrganismMetadata> organismMetadataQueue;

	private EUtils eutils;

	public OrganismGeneIdFetcher(EUtils eutils)
	{
		this.eutils = eutils;
	}

	public void getOrganismGeneIds(Object metadata) throws Exception
	{
		OrganismMetadata organismMetadata = (OrganismMetadata)metadata;
		organismMetadata.setGeneIds(eutils.getGeneIds(organismMetadata));
	}
}
