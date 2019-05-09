// $Id: PopulateCacheTask.java,v 1.13 2012/02/24 20:12:27 grossb Exp $
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
package org.mskcc.cbio.biogene.task;

// imports
import org.mskcc.cbio.biogene.util.cache.EhCache;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import java.util.Properties;
import java.util.ArrayList;
import java.io.File;

/**
 * Task to populate the biogene server cache.
 *
 * @author Benjamin Gross
 */
public class PopulateUniProtMappingCacheTask extends Thread {

    private static Logger log = Logger.getLogger(PopulateUniProtMappingCacheTask.class);
	private File uniProtMappingFile;

    /**
     * Constructor.
     */
    public PopulateUniProtMappingCacheTask() throws Exception {

		// init uniProtMappingFile
		Properties props = new Properties();
		props.load(PopulateUniProtMappingCacheTask.class.getResourceAsStream("/biogene.properties"));
		uniProtMappingFile = FileUtils.getFile(props.getProperty("uniprot.mapping.path"));
    }

    /**
     * Runs the Task.
     */
    public void run() {

		if (log.isInfoEnabled()) {
			log.info("PopulateUniProtMappingCacheTask()...");
		}

		LineIterator it = null;
        try {
			it = FileUtils.lineIterator(uniProtMappingFile);
			while (it.hasNext()) {
				String[] parts = it.nextLine().split("\t");
				EhCache.storeUniProtMappingInCache(parts[0], parts[1]);
			}
        }
		catch (Exception e) {
			return;
		}
		finally {
			it.close();
		}

		if (log.isInfoEnabled()) {
			log.info("PopulateUniProtMappingCacheTask() complete...");
		}
    }
}
