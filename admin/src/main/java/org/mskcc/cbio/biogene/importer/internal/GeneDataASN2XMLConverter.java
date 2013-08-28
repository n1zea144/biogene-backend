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

import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.scope.context.ChunkContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.List;
import java.util.HashMap;

public class GeneDataASN2XMLConverter implements Tasklet
{
	private static Log logger = LogFactory.getLog(GeneDataFetcher.class);
	private static final String GENE_DATA_REGEX = "<GENE-DATA-FILE>";

	@Autowired
	@Qualifier("geneFileMetadata")
	private HashMap<GeneFileMetadata,GeneFileMetadata> geneFileMetadata;


	private Config config;
	private String gene2XmlBin;
	private File localDirectory;

	public GeneDataASN2XMLConverter(Config config, String gene2XmlBin, String geneDataDestDirectory)
	{
		this.config = config;
		this.gene2XmlBin = gene2XmlBin;
		localDirectory = new File(geneDataDestDirectory);
		logger.info("local directory: " + localDirectory);
	}

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception
    {
		for (GeneFileMetadata metadata : geneFileMetadata.keySet()) {
			if (metadata.importFile()) {
				File geneDataFile = FileUtils.getFile(localDirectory, metadata.getFilename());
				logger.info("converting gene data: " + geneDataFile.getCanonicalPath());
				String convertCommand = getConvertCommand(geneDataFile);
				logger.info("executing: " + convertCommand);
				execute(convertCommand);
			}
		}
        return RepeatStatus.FINISHED;
    }

	private String getConvertCommand(File geneDataFile) throws Exception
	{
		return gene2XmlBin.replaceAll(GENE_DATA_REGEX, geneDataFile.getCanonicalPath());
	}

	private void execute(String command) throws Exception
	{
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		if (process.exitValue() != 0) throw new RuntimeException();
	}
}
