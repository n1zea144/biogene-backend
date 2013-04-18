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
import org.apache.log4j.Logger;

import org.mskcc.cbio.biogene.schema.*;
import org.mskcc.cbio.biogene.eutils.*;
import org.mskcc.cbio.biogene.util.cache.EhCache;

import java.util.ArrayList;

/**
 * Task to populate the biogene server cache.
 *
 * @author Benjamin Gross
 */
public class PopulateCacheTask extends Thread {

	// some statics
	public static int DEFAULT_RETSTART = 0;
	public static int DEFAULT_EUTIL_DELAY = 5000;
	public static int DEFAULT_TOTAL_NUMBER_TO_FETCH = -1;
	public static int DEFAULT_NUMBER_TO_FETCH_PER_REQUEST = 50;

	private static int NUMBER_OF_RETRIES = 3;
	private static int RETRY_DELAY = 1000 * 60 * 5; // five minutes
    private static Logger log = Logger.getLogger(PopulateCacheTask.class);

    // some members
    private String organism;
	private int retStart;
    private int numberToFetchPerRequest;
    private int totalNumberToFetch;
    private int eUtilDelay;

    /**
     * Constructor.
     *
	 * retStart is starting id number.
     * numberToFetchPerRequest is the number of id's to get from ncbi- entrez gene in one request.  totalNumberToFetch
     * is the maximum number of id's to fetch from ncbi - entrez gene.  Set this to -1 to grab all ids available 
     * for given organism.  eutilDelay is time to wait between subsequent fetches, in milliseconds
	 *
	 * @param organism String
	 * @param retStart int
	 * @param numberToFetchPerRequest int
	 * @param totalNumberToFetch int
     * @param eUtilDelay int
     *
     * @param consoleMode Running in Console Mode.
     * @param xdebug      XDebug Object.
     */
    public PopulateCacheTask(String organism, int retStart, int numberToFetchPerRequest, int totalNumberToFetch, int eUtilDelay) {

        // init members
        this.organism = organism;
		this.retStart = (retStart < 0) ? DEFAULT_RETSTART : retStart;
        this.numberToFetchPerRequest = (numberToFetchPerRequest <= 0) ? DEFAULT_NUMBER_TO_FETCH_PER_REQUEST : numberToFetchPerRequest;
        this.totalNumberToFetch = (totalNumberToFetch == 0) ? DEFAULT_TOTAL_NUMBER_TO_FETCH : totalNumberToFetch;
        this.eUtilDelay = (eUtilDelay <= 0) ? DEFAULT_EUTIL_DELAY : eUtilDelay;
    }

    /**
     * Runs the Task.
     */
    public void run() {
        try {
            populateCache();
        } catch (Exception e) {
            return;
        }
    }

	/**
	 * Populates cache with entrez gene data for given organism.
	 */
	private void populateCache() throws Exception {

        long startTimeOfFetch = 0;
		int eUtilDelayOrig = eUtilDelay;
		eUtilDelay = 0;
        boolean checkedTotalNumberToFetch = false;
		if (log.isInfoEnabled()) {
			log.info("PopulateCacheTask.populateCache(), organism: " + organism +
					 "; numberToFetchPerRequest: " + numberToFetchPerRequest + "; totalNumberToFetch: " + totalNumberToFetch);
		}

		// sanity checks
		if (totalNumberToFetch >= 0 && totalNumberToFetch < numberToFetchPerRequest) {
			numberToFetchPerRequest = totalNumberToFetch;
		}

		int numberProcessed = 0;
		int numberOfRetries = 0;
		do {
			if (log.isInfoEnabled()) {
				log.info("PopulateCacheTask.populateCache(), total to fetch: " + totalNumberToFetch);
			}

            // if necessary, delay before fetch
            if (eUtilDelay > 0) {
                boolean loggedMesg = false;
                while (System.currentTimeMillis() - startTimeOfFetch < eUtilDelay) {
                    if (!loggedMesg) {
						if (log.isInfoEnabled()) {
							log.info("PopulateCacheTask.populateCache(), sleeping...");
						}
                        loggedMesg = true;
                    }
                    sleep(100);
                }
                if (loggedMesg == true) {
					if (log.isInfoEnabled()) {
						log.info("PopulateCacheTask.populateCache(), waking up...");
					}
                }
            }

            // set start time of fetch
            if (eUtilDelay > 0) {
                startTimeOfFetch = System.currentTimeMillis();
            }

			// get ids
			ArrayList<String> IDs = new ArrayList<String>();
			if (log.isInfoEnabled()) {
				log.info("PopulateCacheTask.populateCache(), call grabbing " + numberToFetchPerRequest  + " IDs, starting at " + retStart + numberProcessed);
			}
			Integer[] idCount = new Integer[1];
			EUtils.RETURN_CODES code = ESearch.search(organism, retStart + numberProcessed, numberToFetchPerRequest, IDs, idCount);
			if (code != EUtils.RETURN_CODES.SUCCESS) {
				if (log.isInfoEnabled()) {
					log.info("PopulateCacheTask.populateCache(), error executing ESearch.search(), return code: " + code.toString());
				}
				if (++numberOfRetries <= NUMBER_OF_RETRIES) {
					if (log.isInfoEnabled()) {
						log.info("PopulateCacheTask.populateCache(), retrying fetch in five minutes (numberOfRetries: " + numberOfRetries + ")");
					}
					// lets jack up delay to 5 minutes
					startTimeOfFetch = System.currentTimeMillis();
					eUtilDelay = RETRY_DELAY;
					continue;
				}
				if (log.isInfoEnabled()) {
					log.info("PopulateCacheTask.populateCache(), no more retries available, exiting task, total number processed: " + numberProcessed);
				}
				return;
			}

			// made it here, reset number of retries & eUtilDelay
			numberOfRetries = 0;
			eUtilDelay = eUtilDelayOrig;

            // break if no ids to process
			if (IDs.size() == 0) {
				if (log.isInfoEnabled()) {
					log.info("PopulateCacheTask.populateCache(), IDs.size() == 0, exiting task");
				}
				return;
			}

			// use ids to fetch rifs
            GeneInfoList geneInfoList = EFetch.fetch(IDs);

			// store in cache
			if (geneInfoList.getReturnCode().equals(EUtils.RETURN_CODES.SUCCESS.toString())) {
				for (GeneInfo geneInfo : geneInfoList.getGeneInfo()) {
					EhCache.storeInfoInCache(geneInfo);
				}
			}

            // check that desired number of ids to fetch
            // is <= number of ids available to retreive based on query (organism)
            if (!checkedTotalNumberToFetch) {
                if (totalNumberToFetch < 0) {
                    totalNumberToFetch = idCount[0];
                }
                else if (idCount[0] < totalNumberToFetch) {
					if (log.isInfoEnabled()) {
						log.info("PopulateCacheTask.populateCache(), number ids available: " + idCount[0] +
								 " is less than number id's requested: " + totalNumberToFetch + " .  Adjusting number requested.");
					}
                    totalNumberToFetch = idCount[0];
                }
                checkedTotalNumberToFetch = true;
            }

			// update number processed
			numberProcessed += IDs.size();
			if (log.isInfoEnabled()) {
				log.info("PopulateCacheTask.populateCache(), number processed: " + numberProcessed);
			}

			// reduce number of records to fetch per request if the # left to fetch < numberToFetchPerRequest
			if ((totalNumberToFetch - numberProcessed) < numberToFetchPerRequest) {
				numberToFetchPerRequest = totalNumberToFetch - numberProcessed;
			}

		} while (numberProcessed < totalNumberToFetch);

		if (log.isInfoEnabled()) {
			log.info("PopulateCacheTask.populateCache(), complete, total to fetch: "  + totalNumberToFetch + ", total number processed: " + numberProcessed);
		}
	}
}
