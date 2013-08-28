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

import org.mskcc.cbio.biogene.importer.Config;
import org.mskcc.cbio.biogene.importer.model.GeneFileMetadata;

import org.apache.commons.logging.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.collections.CollectionUtils;

import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.scope.context.ChunkContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.file.remote.synchronizer.AbstractInboundFileSynchronizer;

import java.io.File;
import java.util.List;
import java.util.HashMap;

public class GeneDataFetcher implements Tasklet
{
	private static Log logger = LogFactory.getLog(GeneDataFetcher.class);

	@Autowired
	@Qualifier("geneFileMetadata")
	private HashMap<GeneFileMetadata,GeneFileMetadata> geneFileMetadata;

	private Config config;
	private File localDirectory;
	private AbstractInboundFileSynchronizer<?> ftpInboundFileSynchronizer;

	public GeneDataFetcher(Config config, SessionFactory sessionFactory, String geneDataDestDirectory)
	{
		localDirectory = new File(geneDataDestDirectory);
		logger.info("local directory: " + localDirectory);
		ftpInboundFileSynchronizer = new FtpInboundFileSynchronizer(sessionFactory);
		this.config = config;
	}

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception
    {
		if (geneFileMetadata.isEmpty()) getGeneFileMetadata(config);
		for (GeneFileMetadata metadata : geneFileMetadata.keySet()) {
			if (metadata.fetchFile()) {
				String remoteFile = metadata.getFilename();
				logger.info("fetching gene data: " + metadata.getPath() + ":" + remoteFile);
				deleteLocalFile(remoteFile);
				((FtpInboundFileSynchronizer)ftpInboundFileSynchronizer).setFilter(new FtpSimplePatternFileListFilter(remoteFile));
				ftpInboundFileSynchronizer.setRemoteDirectory(metadata.getPath());
				ftpInboundFileSynchronizer.synchronizeToLocalDirectory(localDirectory);
			}
		}
        return RepeatStatus.FINISHED;
    }

	private void deleteLocalFile(String filename)
    {
		SimplePatternFileListFilter filter = new SimplePatternFileListFilter(filename);
		List<File> matchingFiles = filter.filterFiles(localDirectory.listFiles());
		if (CollectionUtils.isNotEmpty(matchingFiles)) {
			for (File file : matchingFiles) {
				FileUtils.deleteQuietly(file);
			}
		}
    }

	private void getGeneFileMetadata(Config config)
	{
		for (GeneFileMetadata metadata : config.getGeneFileMetadata()) {
			geneFileMetadata.put(metadata, metadata);
		}
	}
}
