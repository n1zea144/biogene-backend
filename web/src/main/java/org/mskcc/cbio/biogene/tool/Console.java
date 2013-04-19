// $Id
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
package org.mskcc.cbio.biogene.tool;

// imports
import org.mskcc.cbio.biogene.eutils.EUtils;
import org.mskcc.cbio.biogene.task.PopulateCacheTask;
import org.mskcc.cbio.biogene.task.PopulateUniProtMappingCacheTask;
import org.mskcc.cbio.biogene.util.cache.EhCache;

import org.apache.log4j.Logger;

import java.util.Properties;

import java.io.IOException;
import java.io.FileInputStream;

/**
 * Master utility tool to export all cPath data.
 *
 * @author Benjamin Gross
 */
public class Console {

	// some statics
    private static Logger log = Logger.getLogger(Console.class);

	// members
	private int retStart;
	private int totalNumberToFetch;
	private int numberToFetchPerRequest;
    private int eUtilDelay;
	private String[] organisms;
    private Boolean populateCache;

	/**
	 * Constructor.
	 */
	public Console(String organismStr, int retStart, int numberToFetchPerRequest, int totalNumberToFetch, int eUtilDelay) {

		// set members
		if (log.isDebugEnabled()) {
			log.debug("Console(), reading organisms: " + organismStr);
		}
		if (organismStr.contains(":")) {
			this.organisms = organismStr.split(":");
		}
		else {
			this.organisms = new String[1];
			this.organisms[0] = organismStr;
		}

		if (log.isDebugEnabled()) {
			log.debug("Console(), reading retstart: " + retStart);
		}
		this.retStart = retStart;

		if (log.isDebugEnabled()) {
			log.debug("Console(), reading number_to_fetch_request: " + numberToFetchPerRequest);
		}
		this.numberToFetchPerRequest = numberToFetchPerRequest;
		
		if (log.isDebugEnabled()) {
			log.debug("Console(), reading total_number_to_fetch: " + totalNumberToFetch);
		}
		this.totalNumberToFetch = totalNumberToFetch;

		if (log.isDebugEnabled()) {
			log.debug("Console(), reading eutil_delay: " + eUtilDelay);
		}
        this.eUtilDelay = eUtilDelay;

        // it is implied by calling this constructor that we want to populate cache
        this.populateCache = new Boolean(true);
	}

    /**
     * Method which returns populate cache property.
     *
     * @return Boolean
     */
    public Boolean getPopulateCacheProperty() {
        return populateCache;
    }

    /**
     * Method to initialize cache.
     */
    public static void initializeCache() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Console.intializeCache(), initializing cache...");
		}
		EhCache.initCache();
    }

    /**
     * Method to initialize cache.
	 *
	 * @param resetCache boolean
     */
    public static void clearCache() throws Exception {
		EhCache.resetAllCaches();
	}
																							   
	/**
	 * Method to populate biogene proxy cache.
	 * Only cache records of species we are interested in.
	 */
	public void populateCache() throws Exception {

        if (populateCache) {
			if (log.isDebugEnabled()) {
				log.debug("Console.populateCache(), spawning threads...");
			}
            for (String organism : organisms) {
				if (log.isDebugEnabled()) {
					log.debug("Console.populateCache(), processing organism: " + organism);
				}
                PopulateCacheTask task = new PopulateCacheTask(organism, retStart, numberToFetchPerRequest, totalNumberToFetch, eUtilDelay);
                task.start();
            }
        }
	}

	public static void populateUniProtCache() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Console.populateUniProtCache()...");
		}
		(new PopulateUniProtMappingCacheTask()).start();
	}

    /**
     * The big deal main.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

		// branch based on command
		String command = args[0];
		if (log.isDebugEnabled()) {
			log.debug("Console.main(), command is: " + command);
		}

		if (command.equals("p")) {
            if (args[1] == null) {
                System.out.println("command '" + command +
                                   "' requires options: <organism[:organism],ret_start,number_to_fetch_per_request,total_number_to_fetch,eutil_delay");
                System.exit(1);
            }
            String[] commandOptions = args[1].split(",");
			if (log.isDebugEnabled()) {
				log.debug("Console.main(), command options: " + args[1]);
			}
			Console console = new Console(commandOptions[0],
										  new Integer(commandOptions[1]),
                                          new Integer(commandOptions[2]),
                                          new Integer(commandOptions[3]),
                                          new Integer(commandOptions[4]));
			console.populateCache();
		}
        else if (command.equals("i")) {
			Console.initializeCache();
        }
        else if (command.equals("c")) {
			Console.clearCache();
        }
		else if (command.equals("u")) {
			Console.populateUniProtCache();
		}
    }
}