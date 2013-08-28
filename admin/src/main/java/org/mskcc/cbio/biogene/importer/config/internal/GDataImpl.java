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
package org.mskcc.cbio.biogene.importer.config.internal;

import org.mskcc.cbio.biogene.importer.Config;
import org.mskcc.cbio.biogene.importer.model.GeneFileMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.FeedURLFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Class which implements the Config interface
 * using google docs as a backend.
 */
class GDataImpl implements Config {

	private static Log LOG = LogFactory.getLog(GDataImpl.class);

	private String gdataUser;
	private String gdataPassword;
	private SpreadsheetService spreadsheetService;

	ArrayList<ArrayList<String>> geneFilesMatrix;

	private String gdataSpreadsheet;
	private String geneFilesWorksheet;

	public GDataImpl(String gdataUser, String gdataPassword,
					 SpreadsheetService spreadsheetService,String gdataSpreadsheet,
					 String geneFilesWorksheet)
	{
		this.gdataUser = gdataUser;
		this.gdataPassword = gdataPassword;
		this.spreadsheetService = spreadsheetService;
		this.gdataSpreadsheet = gdataSpreadsheet;
		this.geneFilesWorksheet = geneFilesWorksheet;
	}

	@Override
	public Collection<GeneFileMetadata> getGeneFileMetadata()
	{

		if (geneFilesMatrix == null) {
			geneFilesMatrix = getWorksheetData(gdataSpreadsheet, geneFilesWorksheet);
		}

		Collection<GeneFileMetadata> geneFileMetadata = 
			(Collection<GeneFileMetadata>)getMetadataCollection(geneFilesMatrix,
																"org.mskcc.cbio.biogene.importer.model.GeneFileMetadata");
		return geneFileMetadata;
	}

	private Collection<?> getMetadataCollection(ArrayList<ArrayList<String>> metadataMatrix, String className)
	{
		Collection<Object> toReturn = new ArrayList<Object>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getMetadataCollection(): " + className);
		}

		// we start at one, because row 0 is the column headers
		for (int lc = 1; lc < metadataMatrix.size(); lc++) {
			Object[] args = { metadataMatrix.get(lc).toArray(new String[0]) };
			try {
				toReturn.add(ClassLoader.getInstance(className, args));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return toReturn;
	}

	private void login() throws Exception
	{
		spreadsheetService.setUserCredentials(gdataUser, gdataPassword);
	}

	private SpreadsheetEntry getSpreadsheet(String spreadsheetName) throws Exception
	{
		FeedURLFactory factory = FeedURLFactory.getDefault();
		SpreadsheetFeed feed = spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry entry : feed.getEntries()) {
			if (entry.getTitle().getPlainText().equals(spreadsheetName)) {
				return entry;
			}
		}
		
		return null;
	}

	private WorksheetEntry getWorksheet(String spreadsheetName, String worksheetName) throws Exception
	{
		SpreadsheetEntry spreadsheet = getSpreadsheet(spreadsheetName);
		if (spreadsheet != null) {
			WorksheetFeed worksheetFeed = spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
				if (worksheet.getTitle().getPlainText().equals(worksheetName)) {
					return worksheet;
				}
			}
		}

		return null;
	}

	private ArrayList<ArrayList<String>> getWorksheetData(String spreadsheetName, String worksheetName)
	{
		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getWorksheetData(): " + spreadsheetName + ", " + worksheetName);
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(spreadsheetName, worksheetName);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				if (feed != null && feed.getEntries().size() > 0) {
					boolean needHeaders = true;
					for (ListEntry entry : feed.getEntries()) {
						if (needHeaders) {
							ArrayList<String> headers = new ArrayList<String>(entry.getCustomElements().getTags());
							toReturn.add(headers);
							needHeaders = false;
						}
						ArrayList<String> customElements = new ArrayList<String>();
						for (String tag : toReturn.get(0)) {
							String value = entry.getCustomElements().getValue(tag);
							if (value == null) value = "";
							customElements.add(value);
						}
						toReturn.add(customElements);
					}
				}
				else {
					if (LOG.isInfoEnabled()) {
						LOG.info("Worksheet contains no entries!");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return toReturn;
	}
}
